CAP Tests
---


```yml
entity-management:
   entities: [Task, CheckList]
   binder-chain:
   -  DatabaseConnection:
         file: /com/fpetrola/capdemo/hibernate.properties
   -  EntiyExtractor: default
   -  AutoORMMappingGenerator: default
   -  JPAEntityBinder: default
```
