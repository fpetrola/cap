CAP Tests
---


```yml
!com.fpetrola.cap.config.ModelManagement
binderChain: 
- !com.fpetrola.cap.model.binders.ConnectionExtractorFromHibernateXML {}
- !com.fpetrola.cap.model.binders.DatabaseEntitiesExtractor
   filters: 
   - Entity[table1]
- !com.fpetrola.cap.model.binders.BasicORMMappingGenerator {}
- !com.fpetrola.cap.model.binders.JPAEntityBinder
   workspacePath: /home/fernando/git/cap-tests
```
