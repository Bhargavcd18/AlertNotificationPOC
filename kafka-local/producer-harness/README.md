This project is a Kafka-based event-driven notification proof of concept (POC) designed to simulate Temenos-generated banking domain events and validate:

Event production

Partition routing

Consumer group isolation

Offset tracking

Rebalance behavior

Notification routing logic readiness

The system models multi-domain banking events and routes them through Kafka topic:

notifications.events
🏗 Architecture Overview
Temenos-Style Event (Simulated)
        ↓
ProducerHarness (Java)
        ↓
Kafka Topic (8 partitions)
        ↓
Consumer Group: notification-cg-sms
Consumer Group: notification-cg-email
        ↓
Notification Routing (Future T1-07)


🔧 Technology Stack
Component	Technology
Messaging	Apache Kafka (KRaft mode)
Containerization	Docker + Docker Compose
Language	Java 21
Build Tool	Maven
Serialization	JSON
JSON Parsing	Jackson
OS	Windows 10


📂 Project Structure
Alert-POC/
│
├── docker-compose.yml
├── create-topics.sh
├── README.md
│
├── producer-harness/
│   ├── pom.xml
│   ├── src/main/java/com/poc/ProducerHarness.java
│   └── src/main/resources/payloads/
│       ├── 01_newaccountcreate.json
│       ├── 02_customer_successful_creation.json
│       ├── 03_lending_loan_disbursement_successful.json
│       ├── 04_lending_pastdue_loan_reminder_90days.json
│       └── 05_tph_fund_transfer_successful.json


🔑 Event Design Principles

Each event follows a Temenos-style CloudEvents envelope.

Core Identifiers Used for Routing
Identifier	Purpose
eventType	Domain classification
application	Module-level routing
MessageType	Channel selection (SMS / EMAIL / BOTH)
alertType	Template resolution


🧾 Supported Domain Events

Domain	alertType
Accounts	ACCOUNT_CREATED
Customer	CUSTOMER_CREATED
Lending	LOAN_DISBURSEMENT_SUCCESSFUL
Lending	LOAN_PAST_DUE_REMINDER
Payments	FUND_TRANSFER_SUCCESSFUL


🔀 Partition Strategy

Kafka message key:

businessKey = POCBank|<CompanyId>|<ArrangementReference>

Benefits:

Stable partition routing

Customer-level grouping

Horizontal scalability

Deterministic ordering per entity

🚀 How to Run

1️⃣ Start Kafka

docker compose up -d

Verify:

docker ps
2️⃣ Verify Topic
docker exec -it kafka-broker kafka-topics --bootstrap-server localhost:9092 --describe --topic notifications.events

Expected:

8 partitions

Replication factor 1

3️⃣ Start Consumer Groups
SMS Consumer
docker exec -it kafka-broker kafka-console-consumer --bootstrap-server localhost:9092 --topic notifications.events --group notification-cg-sms
Email Consumer
docker exec -it kafka-broker kafka-console-consumer --bootstrap-server localhost:9092 --topic notifications.events --group notification-cg-email
4️⃣ Run Producer Harness
mvn clean package
mvn exec:java -Dexec.mainClass="com.poc.ProducerHarness"

Expected Output:

File      : 01_newaccountcreate.json
Key       : POCBank|PH0099001|AAACT260001POCBK9X
Partition : 3
Offset    : 12
🧪 Smoke Test (T1-05)

Validated:

✔ Broker healthy
✔ Topic created
✔ Producer sends
✔ Both consumer groups receive
✔ Offset committed
✔ Lag = 0
✔ Rebalance tested

Check lag:

docker exec -it kafka-broker kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group notification-cg-sms
📊 Validation Results
Test	Result
Partition Routing	Passed
Consumer Isolation	Passed
Offset Tracking	Passed
Lag Monitoring	Passed
Rebalance Behavior	Passed
📈 Scalability Design

Topic: 8 partitions

Multiple consumer groups supported

Idempotent producer enabled

acks=all

Retry strategy configured

Future-ready for horizontal scaling

🔮 Next Phase

Upcoming enhancements:

T1-06 — Spring Boot Actuator integration

T1-07 — Java-based notification consumer

Template resolution engine

SMS / Email dispatch simulation

Prometheus metrics integration

Multi-broker scaling

📌 Current Status
Task	Status
T1-01 — Kafka Setup	✅ Completed
T1-02 — Topic Creation	✅ Completed
T1-03 — Producer Harness	✅ Completed
T1-04 — Payload Samples	✅ Completed
T1-05 — Smoke Test	✅ Completed
T1-06 — Actuator	🔜 Next
🧠 Key Learnings

Partitioning strategy impacts ordering guarantees

Consumer groups provide parallel processing

Lag monitoring is critical for production

Rebalance behavior must be tested early

Routing logic belongs in application layer

👤 Author

POCBank Kafka Notification POC
Backend / Event-Driven Architecture Validation