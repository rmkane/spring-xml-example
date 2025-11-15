.DEFAULT_GOAL := help

# =============================================================================
# Configuration
# =============================================================================
APP_NAME := spring-xml-example
VERSION := 1.0.0-SNAPSHOT
JAR_FILE := target/$(APP_NAME)-$(VERSION).jar
SERVER_PORT := 8080
DEBUG_PORT := 8787
SPRING_BOOT_PID := .spring-boot.pid
JAR_PID := .jar.pid

DEBUG_JVM_ARGS := -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$(DEBUG_PORT)

# =============================================================================
# Helper Functions
# =============================================================================

# Function to stop a process by PID file (kills process tree)
# Usage: $(call stop_process,<pid_file>,<process_name>)
define stop_process
	@if [ -f $(1) ]; then \
		PID=$$(cat $(1)); \
		if ps -p $$PID > /dev/null 2>&1; then \
			echo "Stopping $(2) (PID: $$PID) and child processes..."; \
			kill -TERM $$PID 2>/dev/null || true; \
			sleep 1; \
			if ps -p $$PID > /dev/null 2>&1; then \
				echo "Process still running, force killing..."; \
				kill -9 $$PID 2>/dev/null || true; \
			fi; \
			pkill -P $$PID 2>/dev/null || true; \
			rm -f $(1); \
			echo "$(2) stopped."; \
		else \
			echo "No running $(2) found (PID file exists but process not running)."; \
			rm -f $(1); \
		fi \
	else \
		echo "No PID file found. $(2) may not be running."; \
	fi
endef

# Function to kill process by port
# Usage: $(call kill_by_port,<port>,<process_name>)
define kill_by_port
	@PID=$$(lsof -ti:$(1) 2>/dev/null || echo ""); \
	if [ -n "$$PID" ]; then \
		echo "Found $(2) running on port $(1) (PID: $$PID), killing..."; \
		kill -TERM $$PID 2>/dev/null || true; \
		sleep 1; \
		if lsof -ti:$(1) > /dev/null 2>&1; then \
			echo "Process still running, force killing..."; \
			kill -9 $$PID 2>/dev/null || true; \
		fi; \
		pkill -P $$PID 2>/dev/null || true; \
		echo "$(2) on port $(1) stopped."; \
	else \
		echo "No process found running on port $(1)."; \
	fi
endef

# Function to check if a file exists
# Usage: $(call check_file,<file_path>,<error_message>)
define check_file
	@if [ ! -f $(1) ]; then \
		echo "$(2)"; \
		exit 1; \
	fi
endef

# =============================================================================
# Phony Targets Declaration
# =============================================================================
.PHONY: help \
		clean install package verify \
		run run-debug run-jar \
		stop stop-jar stop-spring-boot \
		test test-integration test-all

# =============================================================================
# Build Targets
# =============================================================================

install: ## Build and install the project (runs unit tests, excludes integration tests)
	mvn clean install

package: ## Build the JAR package (runs unit tests, excludes integration tests)
	mvn clean package

verify: ## Run all tests and verify the build
	mvn clean verify -Pintegration-tests

clean: ## Clean the project (removes target directory and PID files)
	@echo "Cleaning project..."
	@rm -f $(SPRING_BOOT_PID) $(JAR_PID)
	mvn clean
	@echo "Clean complete."

# =============================================================================
# Run Targets
# =============================================================================

run: ## Run the Spring Boot application using Maven
	@echo "Starting Spring Boot application..."
	@mvn spring-boot:run & echo $$! > $(SPRING_BOOT_PID)
	@echo "Spring Boot started with PID: $$(cat $(SPRING_BOOT_PID))"
	@echo "To stop, run: make stop-spring-boot"

run-debug: ## Run the Spring Boot application with debug port $(DEBUG_PORT)
	@echo "Starting Spring Boot application in debug mode (port $(DEBUG_PORT))..."
	@mvn spring-boot:run -Dspring-boot.run.jvmArguments="$(DEBUG_JVM_ARGS)" & echo $$! > $(SPRING_BOOT_PID)
	@echo "Spring Boot started in debug mode with PID: $$(cat $(SPRING_BOOT_PID))"
	@echo "Debugger listening on port $(DEBUG_PORT)"
	@echo "To stop, run: make stop-spring-boot"

run-jar: ## Run the application from the built JAR file
	@if [ ! -f $(JAR_FILE) ]; then \
		echo "JAR file not found. Building package..."; \
		mvn package; \
	fi
	@echo "Starting application from JAR..."
	@java -jar $(JAR_FILE) & echo $$! > $(JAR_PID)
	@echo "Application started with PID: $$(cat $(JAR_PID))"
	@echo "To stop, run: make stop-jar"

# =============================================================================
# Stop Targets
# =============================================================================

stop: stop-spring-boot stop-jar ## Stop all running applications

stop-spring-boot: ## Stop the Spring Boot application started with Maven
	$(call stop_process,$(SPRING_BOOT_PID),Spring Boot application)
	@echo "Checking for processes on port $(SERVER_PORT)..."
	$(call kill_by_port,$(SERVER_PORT),Spring Boot application)

stop-jar: ## Stop the application running from JAR
	$(call stop_process,$(JAR_PID),Application)
	@echo "Checking for processes on port $(SERVER_PORT)..."
	$(call kill_by_port,$(SERVER_PORT),Application)

# =============================================================================
# Test Targets
# =============================================================================

test: ## Run unit tests only (excludes integration tests)
	mvn test

test-integration: ## Run integration tests only
	mvn test -Pintegration-tests

test-all: ## Run all tests (unit + integration)
	mvn test -Pintegration-tests

# =============================================================================
# Help Target
# =============================================================================

help: ## Show this help message
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
