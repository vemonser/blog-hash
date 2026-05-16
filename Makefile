# ══════════════════════════════════════════════════════════════════
# Makefile — Shortcuts للـ Common Commands
# ══════════════════════════════════════════════════════════════════
# الاستخدام: make <command>
# مثال: make up
# ══════════════════════════════════════════════════════════════════

.PHONY: up down restart logs clean psql redis-cli

# ─────────────────────────────────────────────
# Docker Compose Commands
# ─────────────────────────────────────────────

# بدء كل الـ infrastructure services
up:
	docker compose up -d
	@echo "✅ Services started!"
	@echo "   PostgreSQL → localhost:5432"
	@echo "   Redis      → localhost:6379"
	@echo "   MailHog    → http://localhost:8025"
	@echo "   pgAdmin    → http://localhost:5050"

# إيقاف كل الـ services (data بتفضل)
down:
	docker compose down

# إيقاف + حذف الـ volumes (fresh start، data بتروح)
clean:
	docker compose down -v
	@echo "🗑️  All data deleted — fresh start"

# restart service معين: make restart service=postgres
restart:
	docker compose restart $(service)

# متابعة logs: make logs service=postgres
logs:
	docker compose logs -f $(service)

# ─────────────────────────────────────────────
# Database Shortcuts
# ─────────────────────────────────────────────

# فتح psql shell مباشرة في الـ postgres container
psql:
	docker compose exec postgres psql -U postgres -d bloghashdb

# فتح redis-cli
redis-cli:
	docker compose exec redis redis-cli

# ─────────────────────────────────────────────
# Status
# ─────────────────────────────────────────────
status:
	docker compose ps
