# External DNS Operator for Kubernetes
External DNS Operator for Kubernetes is a Kubernetes operator that manages external 
DNS records for Kubernetes services.

The focus is on making it easy to configure and use.
# Getting Started
## Prepare the credentials for the DNS provider
Download the sample secret file and fill in the required credentials for your DNS provider:
```shell
curl -o dnscreds.yaml https://raw.githubusercontent.com/EllipticConsulting/kubernetes-external-dns-operator/main/deploy/credsample.yaml
kubectl apply -n edns-operator -f dnscreds.yaml
```

## To install the operator, run the following commands:
```shell
kubectl apply -f https://raw.githubusercontent.com/EllipticConsulting/kubernetes-external-dns-operator/main/deploy/crd.yaml
kubectl apply -f https://raw.githubusercontent.com/EllipticConsulting/kubernetes-external-dns-operator/main/deploy/rbac.yaml
kubectl apply -n edns-operator -f https://raw.githubusercontent.com/EllipticConsulting/kubernetes-external-dns-operator/main/deploy/operator.yaml
```
That's it! The operator is now running in the `edns-operator` namespace.

## To use the operator, add the following annotation to an ingress or service, for example:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kuard-ingress
  annotations:
    elliptic.external.dns/hostname: kuard.acme.com
    elliptic.external.dns/record.type: A
    elliptic.external.dns/ttl: "120"
    elliptic.external.dns/zoneid: "Z013124234"
    elliptic.external.dns/provider: "aws"
    elliptic.external.dns/value: "8.8.8.8"

spec:
  ingressClassName: nginx
  rules:
    - host: kuard.acme.com
      http:
        paths:
          - backend:
              service:
                name: kuard
                port:
                  number: 80
            path: /
            pathType: Prefix
```

# Reporting an issue
If you believe you have discovered a bug or need some help, please open an
[issue](https://github.com/EllipticConsulting/kubernetes-external-dns-operator/issues). 
Please remember to provide a good summary, description as well as steps to reproduce the issue.
