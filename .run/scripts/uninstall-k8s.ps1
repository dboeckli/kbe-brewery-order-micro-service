cd target/helm/repo

$file = Get-ChildItem -Filter kbe-brewery-order-micro-service-v*.tgz | Select-Object -First 1
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name

helm uninstall $APPLICATION_NAME --namespace kbe-brewery-order-micro-service
