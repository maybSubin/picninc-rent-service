# 🧺 피크닉 용품 예약 및 대여 서비스

텐트 또는 돗자리를 선택하고, 피크닉 테이블·체어·무드등 등의 감성 소품을 추가하여 원하는 날짜에 예약하고 대여할 수 있는 서비스입니다.

## 배포 주소

**https://picninc-rent-service-production.up.railway.app**

- 용품 목록: https://picninc-rent-service-production.up.railway.app/api/items
- Postman 컬렉션의 `baseUrl` 변수를 위 주소로 바꾸면 배포 서버로 요청할 수 있습니다.

## 사용 기술

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- MySQL 8.0
- MongoDB (Spring Data MongoDB)
- Gradle

## 주요 기능

- 피크닉 용품 등록 및 조회
- 이용 날짜별 예약 가능 수량 조회
- 기본 용품 및 추가 소품 예약 (기본 용품 선택 검증, 가격 계산, 날짜별 재고 확인)
- 예약 취소
- 예약한 용품 대여 및 반납 처리
- MongoDB를 활용한 예약·대여 활동 로그 저장 및 조회

## 이용 안내

- 기본 용품은 텐트 또는 돗자리 중 한 종류를 1개 선택합니다.
- 추가 소품은 선택 사항이며 용품별 수량을 지정할 수 있습니다.
- 하루 단위로 대여하며, 이용 당일 수령하고 반납합니다.
- 로그인과 프론트엔드 없이 REST API로 이용합니다.

### 추가 소품

- 피크닉 테이블
- 캠핑 랜턴 / 감성 무드등
- 캠핑 체어 / 릴렉스 체어
- 피크닉 웨건
- 라탄 바구니
- 레이어드용 체크 패턴 피크닉 매트
- 와인잔 세트
- 블루투스 스피커
- 담요

## 실행 방법

### 1. 실행 환경

- Java 17
- MySQL 8.0
- MongoDB

### 2. 데이터베이스 설정

MySQL에 데이터베이스를 생성합니다.

```sql
CREATE DATABASE picnic;
```

`src/main/resources/application.yml`에 접속 정보를 설정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/picnic
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update

  data:
    mongodb:
      uri: mongodb://localhost:27017/picnic
```

실행 환경에 `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 등록합니다.

### 3. 애플리케이션 실행

MySQL과 MongoDB를 실행한 뒤, 프로젝트 루트에서 실행합니다.

```bash
# macOS / Linux
./gradlew bootRun

# Windows
.\gradlew.bat bootRun
```

실행 후 `http://localhost:8080`을 기준으로 Postman에서 API를 호출할 수 있습니다.

- 처음 실행하면 용품 테이블이 비어 있을 때 **샘플 용품 13종**(텐트·돗자리 + 추가 소품 11종)이 자동 등록됩니다.
  (`picnic.sample-data.enabled: false`로 끌 수 있습니다.)
- `postman/picnic-rental.postman_collection.json`을 Postman에 Import 하면 데모 요청을 순서대로 실행할 수 있습니다.
  이용 날짜는 요청 시점의 오늘 날짜(KST)가 자동으로 들어갑니다.

### 4. 테스트

H2 인메모리 DB로 실행되며 MySQL·MongoDB 없이 동작합니다.

```bash
./gradlew test
```

## 배포 (Railway)

`Dockerfile`과 `railway.json`이 포함되어 있어 GitHub 저장소를 연결하면 자동으로 빌드·배포됩니다.

1. [Railway](https://railway.com)에서 **New Project → Deploy from GitHub repo**로 이 저장소를 선택합니다.
2. 같은 프로젝트에서 **+ Create → Database**로 **MySQL**과 **MongoDB**를 각각 추가합니다.
3. 앱 서비스의 **Variables**에 아래 값을 등록합니다. (`${{...}}`는 Railway가 DB 접속 정보로 자동 치환합니다.)

   | 변수 | 값 |
   |---|---|
   | `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?allowPublicKeyRetrieval=true&useSSL=false` |
   | `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
   | `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
   | `MONGO_URI` | `${{MongoDB.MONGO_URL}}` |

4. 앱 서비스의 **Settings → Networking → Generate Domain**으로 공개 주소를 발급받습니다.
5. 발급받은 주소를 Postman 컬렉션의 `baseUrl` 변수에 넣으면 배포된 서버로 요청할 수 있습니다.

| 환경 변수 | 기본값 (로컬) | 설명 |
|---|---|---|
| `PORT` | `8080` | Railway가 자동 주입 |
| `DB_URL` | `jdbc:mysql://localhost:3306/picnic` | MySQL JDBC URL |
| `DB_USERNAME`, `DB_PASSWORD` | (필수) | MySQL 계정 |
| `MONGO_URI` | `mongodb://localhost:27017/picnic` | MongoDB 접속 URI |
| `MONGO_DATABASE` | `picnic` | MongoDB 데이터베이스 이름 |

## 엔티티 설계 (MySQL / JPA)

```
Item (용품) 1 ──── N ReservationItem (예약 항목) N ──── 1 Reservation (예약)
```

| 엔티티 | 주요 필드 | 설명 |
|---|---|---|
| `Item` | name, type(`BASIC`/`OPTION`), price, stock, description | 용품. `BASIC`=텐트·돗자리, `OPTION`=추가 소품. stock은 하루 기준 보유 수량 |
| `Reservation` | customerName, phone, useDate, status, totalPrice, createdAt / canceledAt / rentedAt / returnedAt | 예약. `ReservationItem`과 **1:N** (`cascade = ALL`) |
| `ReservationItem` | reservation, item, quantity, unitPrice, subtotal | 예약에 포함된 용품. 예약 시점 단가를 저장해 이후 가격 변경에 영향받지 않음 |

## MongoDB 활용

`reservation_logs` 컬렉션에 예약 활동 로그를 저장합니다.

- 예약 생성·취소·대여·반납이 **MySQL 트랜잭션 커밋에 성공한 뒤에만** 로그를 저장합니다.
  (`@TransactionalEventListener(phase = AFTER_COMMIT)`) — 실패한 요청은 로그가 남지 않습니다.
- 로그 저장에 실패해도 예약 처리 자체에는 영향을 주지 않습니다.
- `GET /api/reservations/{id}/logs`로 예약별 이력을 시간순으로 조회합니다.

```json
{
  "id": "6af6baff55e0682c6e515736",
  "reservationId": 1,
  "action": "RENTED",
  "fromStatus": "RESERVED",
  "toStatus": "RENTED",
  "customerName": "김피크",
  "useDate": "2026-11-13",
  "totalPrice": 46000,
  "message": "용품 수령(대여 시작)",
  "createdAt": "2026-11-13T16:23:43.467"
}
```

## API 목록

| # | Method | URL | 설명 |
|---|---|---|---|
| 1 | POST | `/api/items` | 용품 등록 |
| 2 | GET | `/api/items?type=BASIC` | 용품 목록 (type 생략 시 전체) |
| 3 | GET | `/api/items/{itemId}` | 용품 상세 |
| 4 | GET | `/api/items/availability?date=2026-11-20` | 날짜별 용품 예약 가능 수량 |
| 5 | POST | `/api/reservations` | 예약 생성 |
| 6 | GET | `/api/reservations/{reservationId}` | 예약 상세 |
| 7 | PATCH | `/api/reservations/{reservationId}/cancel` | 예약 취소 |
| 8 | PATCH | `/api/reservations/{reservationId}/rent` | 대여 (용품 수령) |
| 9 | PATCH | `/api/reservations/{reservationId}/return` | 반납 |
| 10 | GET | `/api/reservations/{reservationId}/logs` | 예약 활동 로그 조회 (MongoDB) |

### 요청 예시

**용품 등록** `POST /api/items`

```json
{ "name": "아이스박스", "type": "OPTION", "price": 5000, "stock": 5, "description": "25L 하드 아이스박스" }
```

**예약 생성** `POST /api/reservations`

```json
{
  "customerName": "김피크",
  "phone": "010-1234-5678",
  "useDate": "2026-11-20",
  "items": [
    { "itemId": 1, "quantity": 1 },
    { "itemId": 3, "quantity": 1 },
    { "itemId": 6, "quantity": 2 }
  ]
}
```

응답 (201 Created)

```json
{
  "id": 1,
  "customerName": "김피크",
  "phone": "010-1234-5678",
  "useDate": "2026-11-20",
  "status": "RESERVED",
  "totalPrice": 46000,
  "items": [
    { "itemId": 1, "name": "감성 텐트", "type": "BASIC", "unitPrice": 30000, "quantity": 1, "subtotal": 30000 },
    { "itemId": 3, "name": "피크닉 테이블", "type": "OPTION", "unitPrice": 8000, "quantity": 1, "subtotal": 8000 },
    { "itemId": 6, "name": "캠핑 체어", "type": "OPTION", "unitPrice": 4000, "quantity": 2, "subtotal": 8000 }
  ],
  "createdAt": "2026-11-13T16:23:43",
  "canceledAt": null,
  "rentedAt": null,
  "returnedAt": null
}
```

## 비즈니스 규칙

### 예약

1. 이용 날짜는 오늘 이후여야 합니다.
2. 기본 용품(`BASIC`)은 **정확히 한 종류를 1개** 선택해야 합니다. (텐트와 돗자리 동시 선택, 2개 이상 선택 불가)
3. 같은 용품을 중복으로 요청할 수 없습니다.
4. **날짜별 재고 확인**: 같은 이용 날짜에 `RESERVED`·`RENTED` 상태인 예약 수량 합계 + 요청 수량이 보유 수량을 넘으면 예약할 수 없습니다.
   취소(`CANCELED`)·반납(`RETURNED`)된 예약은 재고를 점유하지 않습니다.
5. 동시에 같은 용품을 예약하는 경우 용품 행에 비관적 락(`PESSIMISTIC_WRITE`)을 걸어 초과 예약을 막습니다.
6. 총 금액 = Σ(용품 1일 단가 × 수량) — 하루 단위 대여

### 상태 변경

```
RESERVED ─┬─ rent ───▶ RENTED ── return ──▶ RETURNED
          └─ cancel ─▶ CANCELED
```

| 작업 | 허용 상태 | 추가 조건 |
|---|---|---|
| 취소 | `RESERVED` | |
| 대여 | `RESERVED` | 이용 당일에만 가능 |
| 반납 | `RENTED` | |

허용되지 않은 상태에서 요청하면 `409 Conflict`를 반환합니다.

### 에러 응답

```json
{ "code": "OUT_OF_STOCK", "message": "피크닉 웨건의 2026-11-20 예약 가능 수량이 부족합니다. (요청 5개 / 가능 4개)", "timestamp": "..." }
```

| code | HTTP | 설명 |
|---|---|---|
| `INVALID_INPUT` | 400 | 요청 값 검증 실패 |
| `BASIC_ITEM_REQUIRED` | 400 | 기본 용품 선택 규칙 위반 |
| `DUPLICATED_ITEM` | 400 | 같은 용품 중복 요청 |
| `PAST_USE_DATE` | 400 | 지난 날짜 예약 |
| `ITEM_NOT_FOUND` / `RESERVATION_NOT_FOUND` | 404 | 존재하지 않는 리소스 |
| `OUT_OF_STOCK` | 409 | 해당 날짜 재고 부족 |
| `INVALID_STATUS` | 409 | 허용되지 않은 상태 변경 |
| `NOT_USE_DATE` | 409 | 이용 당일이 아닌 날 대여 시도 |

## 패키지 구조

```
com.picnic
├── global        # 공통 예외 처리, Clock, 샘플 데이터
├── item          # 용품 (Item)
├── reservation   # 예약 (Reservation, ReservationItem), 상태 변경, 활동 이벤트
└── log           # MongoDB 활동 로그 (ReservationLog)
```
