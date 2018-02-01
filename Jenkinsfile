#!groovy

//
// Shared values for different stages:
//
def projectName = "pjmeisch-cc-adi-spring-boot-test"
def gitRepoUrl = "https://github.com/pjmeisch-cc/adi-spring-boot-test.git"

// Branches whose Kubernetes resources will never be deleted
persistentBranches = ["master", "staging", "qa", "production"]

// Kubernetes resource time-to-live for non-persistent branches
nonPersistentBranchesTTL = 12 // hours

// the ODP kubernetes cluster to deploy to
def clusterId = 'ys0m9'
// from .git folder or per jenkins Env BRANCH_NAME
def branchName = "unspecified"
// shortened git branch name
def simplifiedBranchName
// will be set later, for  e.g. e2e test
def deployedUrl
// kubernetes namespace, lowercase department abbreviation:
def k8sNamespace = "dof"

def imageName

def currentBranchKubernetesExpiry

// cleanup: only store max 20 for max 15 days, and only 5 artifacts
properties([buildDiscarder([$class: 'EnhancedOldBuildDiscarder', artifactDaysToKeepStr: '', artifactNumToKeepStr: '5', daysToKeepStr: '15', discardOnlyOnSuccess: true, numToKeepStr: '20'])])

@Library('adidas@1.2.0') _

// Determines how long Kubernetes resources for a given branch are kept in the target cluster.
//
// The default behavior treats all branches except those specified in `persistentBranches`
// as temporary development branches.
//
// This must return a valid UNIX timestamp. See docs for the `giantswarm` function for details.
def branchKubernetesExpiry(String branchName) {
    if (branchName in persistentBranches) {
        return "never"
    }
    unixTimestamp.hoursFromNow(nonPersistentBranchesTTL)
}

node {

    stage('Prepare') {
        checkout scm

        pipelineUtils.initEnvironment()

        projectName = metadata.withJenkinsIdSuffix(projectName)

        imageName = metadata.getImageName(k8sNamespace, projectName, GIT_COMMIT)

        currentBranchKubernetesExpiry = branchKubernetesExpiry(GIT_BRANCH)

        currentBuild.displayName = "#$BUILD_NUMBER ($GIT_COMMIT)"
        currentBuild.description = GIT_COMMIT_MESSAGE
    }

    stage('Deploy (dry-run)') {
        giantswarm(
            dryRun: true,
            gitRepoUrl: gitRepoUrl,
            branchName: GIT_BRANCH,
            imageName: imageName,
            simplifiedBranchName: SIMPLIFIED_GIT_BRANCH,
            projectName: projectName,
            commitId: GIT_COMMIT,
            giantswarmClusterId: clusterId,
            logEnvironment: 'dev',
            logLocation: 'aws Ireland',
            k8sNamespace: k8sNamespace,
            teamId: 'dof',
            expires: currentBranchKubernetesExpiry,
            sourceFolder: "src/main/k8s"
        )
    }

    stage('Compile') {
        withMaven(
            maven: 'maven'
        ) {
            sh "mvn -Pci-build -Dmaven.test.failure.ignore clean compile compiler:testCompile"
            gitUtils.createGitProperties('target/classes')
        }
    }

    def testEnvironmentVariables = [
        'K8S_CATALOG_SERVICE_PROTOCOL=http',
        'K8S_CATALOG_SERVICE_HOST=example.com',
        'K8S_CATALOG_SERVICE_PORT=8080'
    ]

    stage('Unit test') {
        withEnv(testEnvironmentVariables) {
            withMaven(
                maven: 'maven'
            ) {
                sh "mvn -Dmaven.test.failure.ignore surefire:test"
            }
            junit '**/target/surefire-reports/TEST-*.xml'
        }
    }
    stage('Coverage') {
        withEnv(testEnvironmentVariables) {
            withMaven(
                maven: 'maven'
            ) {
                sh "mvn -Dmaven.test.failure.ignore test jacoco:report"
            }
            step([$class: 'JacocoPublisher'])
        }
    }
    stage('Integration test') {
        withEnv(testEnvironmentVariables) {
            withMaven(
                maven: 'maven'
            ) {
                sh "mvn compile compiler:testCompile failsafe:integration-test failsafe:verify"
            }
            junit '**/target/failsafe-reports/TEST-*.xml'
        }
    }

    stage('jar') {
        withMaven(
            maven: 'maven'
        ) {
            sh "mvn -DskipTests package"
        }

    }

    stage('Package and publish') {
        dockerUtils.buildAndPush image: imageName, repo: Constants.DOCKER_REGISTRY_AWS, credentials: Constants.DOCKER_REGISTRY_AWS_CREDENTIAL_ID, dockerFileLocation: '-f src/main/docker/Dockerfile .'
    }

    stage('Local Docker cleanup') {
        dockerUtils.deleteOldImages(projectName, GIT_BRANCH)
    }

    stage('Vulnerability scan') {
        def clairResult = clair.scanPushedImage(Constants.DOCKER_REGISTRY_AWS, imageName, Constants.DOCKER_REGISTRY_AWS_CREDENTIAL_ID)
        if (clairResult.vulnerabilities) {
            def clairResultJson = clairResult.json
            clairResult = null // avoid NotSerializableException

            clairResultJsonFile = 'clair-results.json'
            writeFile file: clairResultJsonFile, text: clairResultJson
            archiveArtifacts allowEmptyArchive: true, artifacts: clairResultJsonFile

            echo "Vulnerabilities detected, marking build UNSTABLE"
            currentBuild.result = "UNSTABLE"
        } else {
            clairResult = null // avoid NotSerializableException
            echo "No vulnerabilities detected."
        }
    }

    stage('Deploy') {
        def g = giantswarm(
            gitRepoUrl: gitRepoUrl,
            branchName: GIT_BRANCH,
            imageName: imageName,
            simplifiedBranchName: SIMPLIFIED_GIT_BRANCH,
            projectName: projectName,
            commitId: GIT_COMMIT,
            giantswarmClusterId: clusterId,
            logEnvironment: 'dev',
            logLocation: 'aws Ireland',
            k8sNamespace: k8sNamespace,
            teamId: 'dof',
            expires: currentBranchKubernetesExpiry,
            sourceFolder: "src/main/k8s"
        )

        deployedUrl = g.deployedUrl
        echo "deployed on cluster: ${deployedUrl}"
    }

    stage('Smoke test') {
        def maxTries = 10
        def backOffFactor = 2
        assertions.assertDeployedCommitId(GIT_COMMIT, "${deployedUrl}/_manage/info", maxTries, backOffFactor)
        slackUtils.notify message: "$projectName deployed at $deployedUrl for branch *$GIT_BRANCH*", level: 'info', credentials: 'adi-devops-slack', team: 'adi-devops', channel: 'jenkins-feedback', addBuildInfo: false
    }

    stage('UI Test') {
        withMaven(
            maven: 'maven'
        ) {
            withCredentials([usernamePassword(credentialsId: 'selenium-server', passwordVariable: 'SELENIUM_PASSWORD', usernameVariable: 'SELENIUM_USER')]) {

                sh """
                        set +x
                        export SELENIUM_URL=http://${SELENIUM_USER}:${SELENIUM_PASSWORD}@selenium.dof.4c2tf.k8s.asgard.dub.aws.k8s.3stripes.net/wd/hub
                        export SPRINGBOOT_SEED_URL=$deployedUrl
                        mvn -f ui-test/pom.xml clean test
                    """
            }
        }
    }
    /* Jenkins fails the step if reports are older than a certain time
     * Some report results may not change per run, therefore we run 'touch'
     * 'touch' on existing files, updates the timestamp to current time, thus fixing issue.
     */
    sh 'touch ui-test/target/surefire-reports/TEST-*.xml'
    sh 'touch ui-test/target/cucumber.json'
    junit 'ui-test/target/surefire-reports/TEST-*.xml'
    // Disabled due to Jenkins not having plugin [CucumberTestResultArchiver]
    //step([$class: 'CucumberTestResultArchiver', testResults: 'ui-test/target/cucumber.json'])
    archiveArtifacts allowEmptyArchive: true, artifacts: 'ui-test/target/cucumber/**'

}
