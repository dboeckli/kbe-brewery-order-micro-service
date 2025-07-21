# SFG Beer Works - Brewery Microservices

This project has a services of microservices for deployment via Docker Compose and Kubernetes and is one element of the KBE
See Gateway Project for Detailed description:
https://github.com/dboeckli/kbe-brewery-gateway/blob/master/README.md

This project has been upgraded to spring boot 3.4.1 and not been tested!
Original git repository: https://github.com/springframeworkguru/kbe-sb-microservices.git

## Deployment

### Deployment with Kubernetes

To run maven filtering for destination target/k8s
```bash
mvn clean install -DskipTests 
```

Deployment goes into the default namespace.

To deploy all resources:
```bash
kubectl apply -f target/k8s/
```

To remove all resources:
```bash
kubectl delete -f target/k8s/
```

Check
```bash
kubectl get deployments -o wide
kubectl get pods -o wide
```

You can use the actuator rest call to verify via port 30080

### Deployment with Helm

Be aware that we are using a different namespace here (not default).

To run maven filtering for destination target/helm
```bash
mvn clean install -DskipTests 
```

Go to the directory where the tgz file has been created after 'mvn install'
```powershell
cd target/helm/repo
```

unpack
```powershell
$file = Get-ChildItem -Filter kbe-brewery-order-micro-service-v*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install
```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace kbe-brewery-order-micro-service --create-namespace --wait --timeout 5m --debug --render-subchart-notes
```

show logs
```powershell
kubectl get pods -l app.kubernetes.io/name=$APPLICATION_NAME -n kbe-brewery-order-micro-service
```
replace $POD with pods from the command above
```powershell
kubectl logs $POD -n kbe-brewery-order-micro-service --all-containers
```

test
```powershell
helm test $APPLICATION_NAME --namespace kbe-brewery-order-micro-service --logs
```

uninstall
```powershell
helm uninstall $APPLICATION_NAME --namespace kbe-brewery-order-micro-service
```

delete all
```powershell
kubectl delete all --all -n kbe-brewery-order-micro-service
```

create busybox sidecar
```powershell
kubectl run busybox-test --rm -it --image=busybox:1.36 --namespace=kbe-brewery-order-micro-service --command -- sh
```

You can use the actuator rest call to verify via port 30081

## Contributing
Contributions to improve this template are welcome. Please follow the standard GitHub flow:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request
