📌 **Ticket Reference**

**JIRA:** [JIRA_TICKET_ID](https://aplazo.atlassian.net/browse/JIRA_TICKET_ID)

---

## 🛠️ Summary
<!-- Provide a concise but detailed summary: What changed, why, and impact. Prefer bullets. -->
- 
- 
- 

---

## 🔄 Type of Change
- [ ] Bugfix (non-breaking change that fixes an issue)
- [ ] Feature (non-breaking change that adds functionality)
- [ ] Breaking change (change that breaks backward compatibility)
- [ ] Refactor / Maintenance / Documentation
- [ ] Security improvement
- [ ] Observability / Monitoring

---

## ✅ Changes Included

### Business Logic
- 

### Inter-Service Communication (Clients / Integrations / Messaging)
- 

### Code Hygiene / Refactor
- 

### Observability (Logging / Metrics / Tracing)
- 

### Configuration
- 

### Security
- 

### Testing
- 

---

## 🧪 How was this tested?
<!-- Check what you actually did. If unknown/unverified, leave unchecked and explain below. -->
- [ ] Unit Tests
- [ ] Integration Tests
- [ ] Manual verification (local)
- [ ] Manual verification (staging)
- [ ] Regression checks (critical paths)

**Evidence / Notes**
- Commands executed:
	- `./gradlew test` or `mvn test` (update accordingly)
- Key test cases:
	- 
- Screenshots / logs (if applicable):
	- 

---

## ⚠️ Impact, Risk, and Rollout
<!-- Senior-level signal: clarify risk and how to deploy safely. -->
- **Backward compatible:** Yes / No (explain)
- **Breaking change:** Yes / No (explain)
- **Feature flag:** Yes / No (flag name + default state)
- **Config changes required:** Yes / No (what and where)
- **Data migration:** Yes / No (link to migration / steps)
- **Performance considerations:** (latency, DB load, external calls)
- **Rollout plan:** (safe deploy steps, canary, staged rollout, etc.)
- **Rollback plan:** (how to revert / disable safely)

---

## 📈 Observability & Monitoring
<!-- What should on-call / reviewers watch after deploy? -->
- **Logs updated/added:** Yes / No
- **Metrics/monitors impacted:** (Datadog dashboards/monitors, alerts)
- **Key signals to watch:** (error rate, latency, timeouts, queue lag, etc.)
- **Correlation IDs / tracing notes:** 

---

## 🔗 Dependencies / Related Work
- Related PRs:
	- 
- Dependencies (services, libraries, configs):
	- 
- Follow-ups / TODOs (if any):
	- 

---

## 📎 Notes
<!-- Additional context that helps reviewers and future readers. Keep it detailed but concise. -->
- 

---

## ✅ Checklist
- [ ] My code follows this project’s style guidelines
- [ ] I performed a self-review of my code
- [ ] I added/updated tests where appropriate
- [ ] I updated documentation (if applicable)
- [ ] I verified no new warnings or critical lint issues are introduced
- [ ] I considered security implications (data exposure, authZ/authN, validation)
- [ ] I considered observability (logs/metrics/traces for debugging and monitoring)
