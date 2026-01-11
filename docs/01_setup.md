```markdown
# 01_setup.md - Project Setup and Role-Based Workflow

## Overview

This document establishes the meta-process for a multi-role Java development project using Claude CLI. The project follows a structured workflow with autonomous role switching and comprehensive documentation practices.

## Team Roles

The project operates with four distinct roles:

1. **Product Owner (PO)** - Defines vision, scope, and business requirements
2. **Senior Engineer (SE)** - Provides architectural guidance, technical strategy, and design decisions
3. **Developer (DEV)** - Implements code, follows architectural guidance, and builds features
4. **Q/A Specialist (Q_A)** - Plans tests, executes testing, validates functionality, and ensures quality

Additionally:
- **Operator** - The human participant (you) who answers questions and provides clarifications when needed

## Workflow Sequence

The standard workflow follows this sequence:
```
PO → SE → DEV → Q_A
```

However, all roles are collectively responsible for delivering the final product. If Q/A finds issues:
- Bugs/implementation issues → Loop back to DEV (new step/iteration)
- Design/architecture issues → Loop back to SE (new step/iteration)
- Requirements clarification → Loop back to PO (new step/iteration)

## Autonomous Role Switching

Claude CLI should autonomously switch between roles as the project progresses. When switching roles or thinking:

**Each role must explicitly state their perspective:**
```
[As Product Owner] I need to understand the business requirements...
[As Senior Engineer] Based on the requirements, I recommend...
[As Developer] I will implement the feature using...
[As Q/A Specialist] I will test the following scenarios...
```

## Documentation Structure

### File Naming Convention

All files use zero-padded two-digit step numbers (01, 02, 03, etc.):

1. **`<step>_discussion.md`** - Contains ALL discussions for that step:
   - Questions and answers between roles
   - Questions to the Operator and their answers
   - All English corrected for clarity (fixing typos and grammar)

2. **`<step>_<role>.md`** - Instructions FOR each specific role:
   - `<step>_po.md` - Instructions for Product Owner
   - `<step>_se.md` - Instructions for Senior Engineer
   - `<step>_dev.md` - Instructions for Developer
   - `<step>_q_a.md` - Instructions for Q/A Specialist

3. **`<step>_summary.md`** - Summary of accomplishments for that step:
   - What was decided
   - What was implemented
   - What was tested
   - Current project state

### Step Numbering

- Steps increment sequentially: 01, 02, 03, 04, etc.
- When Q/A finds issues requiring rework, increment to the next step number
- Each complete cycle (PO → SE → DEV → Q/A) typically represents one step
- Iteration loops create new step numbers

## Role Responsibilities

### Product Owner (PO)

**Responsibilities:**
- Define project vision and scope through Q/A session with Operator
- Clarify business requirements and acceptance criteria
- Make business-driven decisions
- Prioritize features and functionality

**Process:**
1. Review previous step documentation (if exists)
2. Conduct Q/A session with Operator until all business requirements are clear
3. Document decisions in `<step>_discussion.md`
4. Create `<step>_se.md` with detailed instructions for Senior Engineer
5. Update `<step>_summary.md`

### Senior Engineer (SE)

**Responsibilities:**
- Review PO's requirements and vision
- Design system architecture
- Recommend technical stack (Java version, frameworks, tools, build systems)
- Provide technical guidance and best practices
- Make architectural decisions

**Process:**
1. Review `<step>_po.md` and previous documentation
2. Ask clarifying questions to PO or Operator as needed
3. Document technical decisions in `<step>_discussion.md`
4. Create `<step>_dev.md` with detailed implementation instructions
5. Update `<step>_summary.md`

**Autonomy:**
- Make reasonable technical decisions independently
- Ask Operator for technical clarifications that may impact execution (Java version, framework preferences)
- Base decisions on project vision from PO Q/A session

### Developer (DEV)

**Responsibilities:**
- Review SE's architectural guidance and design
- Implement features according to specifications
- Write clean, maintainable code
- Follow best practices and coding standards
- Document implementation details

**Process:**
1. Review `<step>_se.md` and previous documentation
2. Ask clarifying questions to SE or Operator as needed
3. Implement code and document decisions in `<step>_discussion.md`
4. Create `<step>_q_a.md` with testing instructions and expected behavior
5. Update `<step>_summary.md`

### Q/A Specialist (Q_A)

**Responsibilities:**
- Review DEV's implementation and test instructions
- Plan comprehensive test strategies
- Execute tests to validate functionality
- Ensure product works as specified
- Document testing best practices
- Identify bugs and issues

**Process:**
1. Review `<step>_dev.md` and previous documentation
2. Create test plan based on requirements
3. Execute tests using Claude CLI capabilities
4. Document test results in `<step>_discussion.md`
5. If issues found:
   - Create `<step+1>_<appropriate_role>.md` for the role that needs to fix the issue
   - Increment step number and start new iteration
6. If all tests pass:
   - Update `<step>_summary.md` with final validation
   - Mark step as complete

**Testing Approach:**
- Test the end product thoroughly
- Document testing best practices
- Ensure all acceptance criteria are met
- Validate both functional and non-functional requirements

## Communication Rules

### Questions to Operator

When roles need clarification from the Operator, format questions clearly:

```
[As <Role>] Operator, <question>?

Example:
[As Senior Engineer] Operator, what Java version should we target for this project?
```

### Inter-Role Communication

When roles need to discuss with each other, document the conversation:

```
[As Senior Engineer] I have a question for the Product Owner regarding the authentication requirements...
[As Product Owner] The authentication should support...
```

### English Correction

All discussions documented in `<step>_discussion.md` should have corrected English, fixing any typos or grammar issues from the Operator's responses.

## Decision-Making Authority

### Autonomous Decisions
Roles should make reasonable decisions independently when:
- Following established best practices
- Making standard technical choices
- Applying common patterns and conventions

### Operator Consultation
Ask the Operator when:
- Business requirements are unclear
- Multiple viable technical approaches exist with trade-offs
- Decisions may impact project execution or constraints
- Clarification needed on scope or priorities

## Quality Standards

Every role must:
- Ask questions continuously until confident they understand completely
- Document all decisions and rationale
- Create clear, detailed instructions for the next role
- Update summaries after completing their work
- Maintain high-quality documentation standards

## Project Initialization

The first step begins with:

1. **Product Owner Q/A Session** (`01_discussion.md`)
   - PO asks Operator about project vision
   - PO asks about scope and requirements
   - PO asks about constraints and success criteria
   - Continue until project vision is crystal clear

2. **Documentation Creation**
   - PO creates `01_se.md` with requirements and context
   - PO creates `01_summary.md` with project overview

3. **Workflow Proceeds**
   - SE reviews and continues the process
   - Each role follows the established pattern

## Technical Stack Considerations

The technical stack will be determined through the workflow:

- **Product Owner** provides business context and constraints
- **Senior Engineer** recommends specific technologies:
  - Java version
  - Framework choices (Spring Boot, Quarkus, etc.)
  - Build tools (Maven, Gradle)
  - Testing frameworks
  - Additional libraries and dependencies
- **Operator** provides input on preferences or constraints when asked

## Success Criteria

A step is considered complete when:
- All roles have completed their responsibilities
- Q/A has validated the implementation
- All tests pass
- Documentation is comprehensive and clear
- `<step>_summary.md` reflects the current state

## Next Steps

After reading this setup document, the **Product Owner** should:

1. Begin the Q/A session with the Operator
2. Understand the project vision and scope
3. Create `01_discussion.md` documenting the conversation
4. Create `01_se.md` with instructions for the Senior Engineer
5. Create `01_summary.md` with the project overview

---

**The project begins with Step 01 - Product Owner Q/A Session**

[As Product Owner] I'm ready to begin the Q/A session with the Operator to understand the project vision and requirements.
```