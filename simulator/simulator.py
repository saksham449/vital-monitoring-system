import json
import os
import random
import time
from datetime import datetime, timezone
from urllib.parse import urlparse

import paho.mqtt.client as mqtt

BEDS = ["BED-01", "BED-02", "BED-03", "BED-04"]
PUBLISH_INTERVAL = 2

# Demo ranges only. These are intentionally synthetic and are not clinical data.
RANGES = {
    "NORMAL": {
        "heart_rate": (65, 95), "spo2": (96, 99), "temperature": (36.4, 37.4),
        "systolic": (110, 130), "diastolic": (70, 85),
    },
    "WARNING": {
        "heart_rate": (80, 105), "spo2": (92, 94), "temperature": (38.1, 38.8),
        "systolic": (135, 145), "diastolic": (85, 95),
    },
    "CRITICAL": {
        "heart_rate": (135, 150), "spo2": (86, 89), "temperature": (38.8, 39.5),
        "systolic": (150, 165), "diastolic": (95, 105),
    },
}


def env(name, default=None):
    return os.getenv(name, default)


def broker_settings():
    raw = env("MQTT_BROKER_URL", "tcp://localhost:1883")
    parsed = urlparse(raw)
    scheme = parsed.scheme or "tcp"
    host = parsed.hostname or raw
    port = int(env("MQTT_PORT", parsed.port or (8883 if scheme in ("ssl", "mqtts") else 1883)))
    return scheme, host, port


def build_client():
    scheme, host, port = broker_settings()
    client = mqtt.Client(client_id=f"vital-simulator-{random.randint(1000, 9999)}")

    username = env("MQTT_USERNAME", "")
    password = env("MQTT_PASSWORD", "")
    if username:
        client.username_pw_set(username, password)

    if scheme in ("ssl", "mqtts"):
        client.tls_set()

    client.connect(host, port, keepalive=60)
    client.loop_start()
    print(f"Simulator connected to MQTT broker {host}:{port}")
    return client


def choose_mode():
    # Approximate demo distribution: 80% normal, 15% warning, 5% critical.
    return random.choices(["NORMAL", "WARNING", "CRITICAL"], weights=[80, 15, 5], k=1)[0]


def reading_for(mode):
    r = RANGES[mode]
    return {
        "heartRate": random.randint(*r["heart_rate"]),
        "spo2": random.randint(*r["spo2"]),
        "temperature": round(random.uniform(*r["temperature"]), 1),
        "systolic": random.randint(*r["systolic"]),
        "diastolic": random.randint(*r["diastolic"]),
    }


def main():
    client = None
    while client is None:
        try:
            client = build_client()
        except Exception as exc:
            print(f"MQTT connection failed: {exc}. Retrying in 5 seconds...")
            time.sleep(5)

    states = {bed: {"mode": "NORMAL", "remaining": random.randint(5, 10)} for bed in BEDS}

    try:
        while True:
            for bed in BEDS:
                state = states[bed]
                if state["remaining"] <= 0:
                    state["mode"] = choose_mode()
                    state["remaining"] = random.randint(5, 10)

                values = reading_for(state["mode"])
                payload = {
                    "bedId": bed,
                    **values,
                    "timestamp": datetime.now(timezone.utc).replace(tzinfo=None).isoformat(timespec="seconds"),
                }
                topic = f"hospital/bed/{bed}/vitals"
                result = client.publish(topic, json.dumps(payload), qos=1)
                if result.rc != mqtt.MQTT_ERR_SUCCESS:
                    print(f"Publish failed for {bed}: rc={result.rc}")
                else:
                    print(
                        f"{bed} -> HR:{values['heartRate']} | SpO2:{values['spo2']} | "
                        f"Temp:{values['temperature']:.1f} | BP:{values['systolic']}/{values['diastolic']} | {state['mode']}"
                    )
                state["remaining"] -= 1

            time.sleep(PUBLISH_INTERVAL)
    except KeyboardInterrupt:
        print("Stopping simulator...")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
