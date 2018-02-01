# Deployment

## Preparation

### <a name="build-docker"></a>Build the docker image
From the root directory of the project, run:

```
DOCKER_IMAGE_NAME=adidas/dof-springboot-seed:master
mvn clean package
docker build -t $DOCKER_IMAGE_NAME -f src/main/docker/Dockerfile .
```

## Run the docker image, locally
`docker run -d -p 8080:8080 -p 8081:8081 $DOCKER_IMAGE_NAME`

- This assumes you want to run it on host port 8080.

### Access the running service
- Point a browser to http://localhost:8080
# Run in Kubernetes (locally with minikube)

## Setup environment

       minikube start                       #1
       eval $(minikube docker-env)          #2
       DOCKER_IMAGE_NAME=adidas/dof-springboot-seed:master #3 Note: this must not contain _ @ / or upper-case letters!

       docker build -t $DOCKER_IMAGE_NAME -f src/main/docker/Dockerfile .

Steps:

1. Start minikube if it's not already running
2. Configure docker client to use the docker instance running in minikube
3. Build the docker image, see above

## Set these additional environment variables for use with `envsubst`
    SERVICE_TYPE=LoadBalancer

    LABEL=adidas-springboot-seed-master
    HOST_NAME_PREFIX=master
    K8S_SERVICE_HOSTNAME=localhost.which.might.not.work.sorry

    export DOCKER_IMAGE_NAME
    export SERVICE_TYPE
    export K8S_SERVICE_HOSTNAME HOST_NAME_PREFIX LABEL

## Create deployment
    cat src/main/k8s/deployment.yml | envsubst | kubectl apply -f -

Note: `deployment.yml` provides the environment variables for other services' endpoints. You can edit those if needed,
search for:
```
        - name: K8S_CATALOG_SERVICE_PROTOCOL
          value: <http/https>
        - name: K8S_CATALOG_SERVICE_HOST
          value: <adidas-springboot-api-seed-master>
        - name: K8S_CATALOG_SERVICE_PORT
          value: "<8080>"
```
## Create service
    cat src/main/k8s/service.yml | envsubst | kubectl apply -f -

## Access App
Access the application in a browser per

    minikube service $LABEL

## Troubleshooting
Minikube provides access the Kubernetes dashboard with the command: `minikube dashboard`
Alternatively, `kubectl`-commands give you detailed information about configuration and state of your cluster, e.g.

    kubectl cluster-info    # check general connection

    kubectl get pods
    kubectl get services
    kubectl get deployments

# Run in Kubernetes (on Giantswarm)

## Build and push Image
1. For pushing to a specific remote registry, the docker image name needs the registry URL prepended.

         IMAGE_USER=quay.io
2. Build the image:

          docker build -t $IMAGE_USER/$DOCKER_IMAGE_NAME -f src/main/docker/Dockerfile .
3. Login to Quay docker registry:

         docker login https://quay.io
4. Push the image to the quay-Registry:

         docker push $IMAGE_USER/$DOCKER_IMAGE_NAME

## Configure Kubectl to point to your Giantswarm Cluster

After obtaining the `kubeconfig` file for your cluster, switch to the respective context using `kubectl config use-context`. Make sure that kubectl is pointing at the correct cluster by running `kubectl cluster-info`.

## Prepare deployment on Giantswarm
1. Set these environment variables and export them in the running shell:

        DOCKER_IMAGE_USER=quay.io
        DOCKER_IMAGE_NAME=$DOCKER_IMAGE_USER/adidas/dof-springboot-seed:master
		DOCKER_IMAGE_REPO=springboot-seed
        SERVICE_TYPE=NodePort

        LABEL=adidas-springboot-seed-master

        HOST_NAME_PREFIX=master

        K8S_SERVICE_HOSTNAME=$HOST_NAME_PREFIX.springboot-seed.dof.4c2tf.k8s.asgard.dub.aws.k8s.3stripes.net
        export DOCKER_IMAGE_NAME
        export DOCKER_IMAGE_REPO
        export SERVICE_TYPE
        export K8S_SERVICE_HOSTNAME HOST_NAME_PREFIX LABEL

1. Make sure you created a kubernetes secret of type `kubernetes.io/dockercfg` with the name
   `quay.io` with the credentials for accessing our current registry at [https://quay.io/organization/adidas](https://quay.io/organization/adidas).
   This can be achieved by following [Creating Image Pull secrets](https://tools.adidas-group.com/confluence/pages/viewpage.action?pageId=190751025)

   Check for its existence in the list of
   `kubectl get secrets`.

## Create or update k8s deployment on Giantswarm
    cat src/main/k8s/deployment.yml | envsubst | kubectl apply --namespace dof -f -
Note: `deployment.yml` provides the environment variables for other services' endpoints. You can edit those if needed,
search for:
```
        - name: K8S_CATALOG_SERVICE_PROTOCOL
          value: <http/https>
        - name: K8S_CATALOG_SERVICE_HOST
          value: <adidas-springboot-api-seed-master>
        - name: K8S_CATALOG_SERVICE_PORT
          value: "<8080>"
```

### Create service (of type **NodePort**)

    cat src/main/k8s/service.yml | envsubst | kubectl apply --namespace dof -f -

### Create Ingress

    cat src/main/k8s/ingress.yml | envsubst | kubectl apply --namespace dof -f -

## Check out your running app on Giantswarm

Each deployed feature branch can be reached at
[https://<branch-name>.springboot-seed.dof.4c2tf.k8s.asgard.dub.aws.k8s.3stripes.net](https://<branch-name>.springboot-seed.dof.4c2tf.k8s.asgard.dub.aws.k8s.3stripes.net)

Assuming your cluster is `4c2tf`, go to [https://master.springboot-seed.dof.4c2tf.k8s.asgard.dub.aws.k8s.3stripes.net](https://master.springboot-seed.dof.4c2tf.k8s.asgard.dub.aws.k8s.3stripes.net)
for the deployed version of the master - branch.
