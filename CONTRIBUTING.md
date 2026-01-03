# Contributing to Family Tree

We love your input! We want to make contributing to Family Tree as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

### Pull Requests

1. Fork the repo and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code follows the existing code style.
6. Issue that pull request!

## Development Setup

1. **Prerequisites**
   - Java 17 or higher
   - Maven 3.6+
   - Neo4j 5.x (or use Docker Compose)

2. **Clone and Setup**
   ```bash
   git clone https://github.com/dharmesh-hemaram/family-tree.git
   cd family-tree
   ```

3. **Start Neo4j**
   ```bash
   docker-compose up neo4j
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

5. **Run Tests**
   ```bash
   mvn test
   ```

## Coding Style

- Follow standard Java coding conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods small and focused
- Use Lombok annotations to reduce boilerplate

## Testing Guidelines

- Write unit tests for service layer logic
- Write integration tests for controllers
- Use MockMvc for controller testing
- Aim for >80% code coverage
- Test edge cases and error conditions

## Commit Messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters or less
- Reference issues and pull requests liberally after the first line

Example:
```
Add ancestor search functionality

- Implement recursive ancestor query in PersonRepository
- Add service method for ancestor retrieval
- Create API endpoint for ancestor search
- Add unit tests for ancestor functionality

Fixes #123
```

## Code Review Process

- All submissions require review
- We may suggest changes, improvements, or alternatives
- Be open to feedback and discussion
- Update your PR based on feedback

## Feature Requests

We use GitHub issues to track feature requests. Great feature requests include:

- A clear and descriptive title
- Detailed description of the proposed functionality
- Why this feature would be useful
- Examples of how it would work

## Bug Reports

We use GitHub issues to track bugs. Great bug reports include:

- A quick summary and/or background
- Steps to reproduce
  - Be specific!
  - Give sample code if you can
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening)

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Questions?

Feel free to open an issue with your question!
