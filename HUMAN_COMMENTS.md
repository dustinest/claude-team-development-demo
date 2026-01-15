# Human Comments

Hi. Human here.

Small disclaimer up front: all code in this repository was written by **Claude Code**. I did not write application code myself. My contribution was limited to guidance, clarification, review, and occasional intervention when something felt off.

The goal of this project was learning. More specifically, to explore how far a multi-role, AI-driven workflow can go when building a slightly more realistic microservices setup. This file exists to capture my human observations, reflections, and lessons learned along the way.

These notes are not chronological. They are observations.

---

## What Worked Well

The most interesting outcome was that **roles actually worked**.

The experiment was designed around the idea that I would write zero code and instead let different roles (Product Owner, Senior Engineer, Developer, Q/A) interact with each other. Each role had a distinct focus, vocabulary, and set of concerns. That separation alone turned out to be surprisingly effective.

Even more interestingly, the system did not collapse immediately into chaos. Discussions happened. Decisions were made. Work was handed over. The flow felt closer to a real team than I expected.

From a learning perspective, that alone already made the experiment worth doing.

---

## Architectural and Process Issues

Some things did not go smoothly, which was honestly not surprising.

During the fifth iteration, Q/A identified a conceptual issue that was escalated to the Senior Engineer. Two services were accessing the same database tables without proper encapsulation, creating a race condition risk.

Looking deeper, it turned out that the architecture included a separate User Signup Service that did not really make sense. Its responsibilities overlapped heavily with the User Service, and its existence introduced unnecessary complexity. At that point, I intervened and asked for the signup logic to be merged into the User Service.

This was a good reminder that speed is not the same as coherence, and that architectural judgment still benefits from a human in the loop.

---

## Time and Effort

My personal time investment was relatively small:

- Around 20 minutes for initial setup
- About 48 minutes discussing requirements and scope with the Product Owner
- Roughly 5 minutes answering Senior Engineer and Developer questions

In total, about 1.5 hours of my time.

The overall project runtime was approximately 1 hour and 57 minutes.


For the amount of structure, discussion, and output produced, this felt efficient.

## Cost and Limits

One thing that became clear very early is that this kind of experiment can get expensive fast.

Within a couple of hours, I managed to exhaust the entire monthly capacity of the Claude subscription (22 EUR). That forced me to wait a few hours for limits to reset. A few days later, I also hit the weekly usage limit, at which point I deliberately drew a line and paused further work.

To properly wrap things up and avoid leaving the project in an unfinished state, I added an extra 10 EUR to my Anthropic pay-per-use account and used that to close the remaining loops.

This was a useful, very concrete reminder that token budgets and usage limits are not abstract constraints. They actively shape how you design workflows, handovers, and stopping points.

---

## Things I Learned (and Would Add Upfront Next Time)

A few lessons only became obvious after something actually broke.

- Do not use absolute paths in documentation or configuration. Relative paths matter more than expected.
- Add a Gradle task to generate documentation for `application.yaml`. This should not be an afterthought.

These are small things, but they remove friction early.

---

## Documentation Observations

Documentation turned out to be more important than I initially assumed.

Claude actually asked for better documentation early on, and I dismissed it as unnecessary at the time. That was a mistake.

A few concrete observations:

- Setup should always be the first documented step. Everything else builds on that.
- It helps to ask the Product Owner to write documentation based on discussions and then hand it over to the Developer. When this is skipped, decisions and implementation details get mixed together.
- Numbered sub-steps would improve readability. Files like `02.1_se.md`, `02.2_dev.md`, or `02.3_discussion.md` might make the pipeline easier to follow.

That said, decision-making documents are a bit special. I do like how free-form discussions currently are, even if they could be structured better.

---

## Flow Breakdowns and Human Intervention

A few times, I had to explicitly ask for things to be documented.

At some point, roles stopped automatically handing work over to the next role. This might have been caused by token limits or context exhaustion. Either way, it required manual nudging.

The positive part is that over time, I learned how to guide the flow more effectively. Once a role completed its work, a simple, well-phrased request was often enough to get things moving again.

---

## Can an LLM Build a Product End-to-End?

While working through this project, one question kept coming back to me: can an LLM actually build a product end-to-end, from an initial idea to something that feels finished?

My current answer is a cautious maybe.

It depends heavily on the complexity of the scope. For simple or well-bounded problems, this approach can work surprisingly well. In this case, however, the scope grew complex enough that I believe a human Senior Engineer would have implemented the core system faster and with higher overall quality.

At this stage, LLMs feel strongest as accelerators. They are very good at producing boilerplate, exploring options, and moving things forward quickly. They are much less reliable at holding a complex system together over time. In practice, the human operator is not optional. The operator is a critical part of the system.

---

## Architecture, Cross-Dependencies, and Cutting Complexity

One architectural moment is worth calling out explicitly.

When a conceptual issue was escalated from Q/A to the Senior Engineer, the proposed solution started to introduce cross-dependencies between services. The account service depended on the user service, and at the same time the user service depended on the account service.

I had a strong feeling that this would lead to yet another iteration cycle. Rather than trying to refine the design further, I chose to simplify aggressively. I asked for the signup logic to be moved directly into the user service and for the standalone signup service to be removed entirely.

This was not about finding the most elegant architecture. It was a pragmatic decision to cut a dependency loop early and keep the system moving forward.

---

## Tests, Q/A, and What I Would Do Differently

One thing I underestimated was the impact of not enforcing test-driven development from day one.

I generally like working with TDD, and in hindsight it would have significantly changed the dynamic between Q/A and the Developer. The lack of early tests caused the code to bounce back and forth more times than necessary. Many of those cycles could likely have been avoided if tests had been in place earlier.

For me, this is a clear note to self. In future experiments like this, asking to practice TDD upfront is not just a preference. It can materially improve both flow and feedback quality.

---

## Current State and Handover

Before continuing, a small but important clarification.

The frontend part of this project is incomplete and may be broken. It was not the focus of this experiment and should be treated accordingly.

More generally, this repository should be viewed as a learning and experimentation artifact, not as production-ready software.

With the last commit, the Product Owner reviewed the progress and formally handed the work over to the Senior Engineer.

To continue the flow, the following prompt is required:

``````
As a Senior Engineer

Remember to estimate your work based on the remaining tokens and plan usage limits.

- Read docs/01_setup.md to understand the multi-role workflow
- Read docs/08_se.md
- Provide sign-off if changes are necessary for the Developer
- Commit your changes
- Ask the Developer to continue with the flow
```
---

## Final Thoughts

This project reinforced one simple idea.

AI-assisted development, at least for me, is not about replacing humans. It is about shifting where human effort matters most. Architecture, judgment, intent, and reflection still seem to benefit from a human in the loop.

For me, this experiment was worth it.