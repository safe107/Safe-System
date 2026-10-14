.PHONY: init web-dev web-build web-test api-dev api-test
init:
	python3 infra/development/init-local.py
web-dev:
	cd apps/web && npm run dev
web-build:
	cd apps/web && npm ci && npm run build
web-test:
	cd apps/web && npm test && npm run lint
api-dev:
	set -a; . ./.local/dev.env; set +a; cd apps/api && ./mvnw spring-boot:run
api-test:
	cd apps/api && ./mvnw verify
