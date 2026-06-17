# Playbook — Deployment 

## Preparation
- Create a branch from `master`
- Make the necessary code changes.
- Update documentation if applicable (ADR, C4,runbooks, etc.).
- Pull Request to master
  - Add valid title: SPACE-123: short description 
  - Add clear description of the change

## Automatic validations
- Jenkins PR Merge 
  - Meets code coverage requirements 
  - No critical issues in SonarQube
- PR Workflow
  - validate title and description

## Merge and tag creation
- Merge PR to master
- update your local master branch
-  create a tag with apz cli:
```sh
 apz tag create
```
## Deployment
- check in jenkins that the build is successful

## Post-deployment
- run the tests in the staging environment
- verify in ECS that the new version is running
-  In case of issues, perform a rollback and review the logs again.