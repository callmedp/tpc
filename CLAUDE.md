# Java All Along — Claude Instructions

## Documentation Rules

Whenever the user asks a question related to a Java topic:

1. **Questions.md** — located at the root `src/` level. Contains only a list of questions (no answers), organized by package.

2. **Theory.md** — one file per package (e.g., `src/multithreading/basics/Theory.md`). Contains every question asked about that package along with its full answer.

### File locations
- `src/Questions.md` — master list of all questions, grouped by package
- `src/multithreading/basics/Theory.md` — Q&A for multithreading basics
- `src/<topic>/<subtopic>/Theory.md` — Q&A for any future topic

### On every new question
- Append the question to `src/Questions.md` under the correct package heading
- Append the question + full answer to the relevant `Theory.md`
- Do this automatically without being asked