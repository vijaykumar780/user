package com.user.user.model;

import lombok.Builder;
import lombok.Data;

/*
{
  "id": "82bc4a37-6a74-45d1-bfd4-e343a5dc8fdc",
  "timestamp": "2025-07-31T05:10:27.460Z",
  "source": "order-api",
  "env": "prod",
  "level": "INFO",
  "type": "event",
  "message": "Order placed successfully",
  "metadata": {
    "userAgent": "Mozilla/5.0",
    "ip": "192.168.1.101"
  },
  "requestId": "req-2837fd3",
  "userId": "user-7423",
  "sessionId": "sess-1892jkf",
  "statusCode": 201,
  "latencyMs": 132
}
 */
@Data
@Builder
public class Event {
    private String id;
    private long timestamp;
    private String path; // api type

    private String userId;
    private String email;
    private int status;
    private long latency;

}
