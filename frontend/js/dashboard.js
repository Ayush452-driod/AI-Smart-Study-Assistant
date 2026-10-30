
/* =========================================
   AUTHENTICATION CHECK
========================================= */

if (!localStorage.getItem("token")) {
    window.location.href = "login.html";
}

let currentMode = "ask";


/* =========================================
   DOM ELEMENTS
========================================= */

const pageTitle = document.getElementById("pageTitle");
const pageSubtitle = document.getElementById("pageSubtitle");
const topicInput = document.getElementById("topicInput");
const generateBtn = document.getElementById("generateBtn");
const result = document.getElementById("result");
const quizOptions = document.getElementById("quizOptions");


/* =========================================
   MARKDOWN CONFIGURATION
========================================= */

marked.setOptions({
    gfm: true,
    breaks: true
});


/* =========================================
   DISPLAY FORMATTED AI RESPONSE
========================================= */

function displayResponse(response) {

    if (!response) {
        result.textContent = "No response received from AI.";
        return;
    }

    const html = marked.parse(String(response));

    result.innerHTML = DOMPurify.sanitize(html);
}


/* =========================================
   SECURE INTERACTIVE QUIZ
========================================= */

async function renderQuiz(quizData) {

    if (
        !quizData ||
        !quizData.quizId ||
        !Array.isArray(quizData.questions) ||
        quizData.questions.length === 0
    ) {
        result.textContent = "Unable to generate quiz.";
        return;
    }

    const questions = quizData.questions;
    const quizId = quizData.quizId;

    result.innerHTML = "";

    let answered = 0;
    let score = 0;

    // Quiz heading
    const heading = document.createElement("h2");
    heading.textContent = "Quiz: " + (quizData.topic || "Practice Quiz");

    // Score
    const scoreDisplay = document.createElement("p");
    scoreDisplay.className = "quiz-score";
    scoreDisplay.textContent =
        `Answered: 0 / ${questions.length} | Score: 0`;

    result.append(heading, scoreDisplay);


    questions.forEach((question, index) => {

        const card = document.createElement("div");
        card.className = "quiz-question-card";

        const title = document.createElement("h3");
        title.textContent = `Question ${index + 1}`;

        const questionText = document.createElement("p");
        questionText.className = "quiz-question-text";
        questionText.textContent = question.question;

        const optionsContainer = document.createElement("div");
        optionsContainer.className = "quiz-answer-options";

        const feedback = document.createElement("div");
        feedback.className = "quiz-feedback hidden";


        question.options.forEach(option => {

            const button = document.createElement("button");

            button.type = "button";
            button.className = "quiz-option-btn";

            button.textContent = `${option.id}. ${option.text}`;
            button.dataset.optionId = option.id;

            button.addEventListener("click", async function () {

                if (optionsContainer.dataset.answered === "true") {
                    return;
                }

                optionsContainer.dataset.answered = "true";

                optionsContainer.querySelectorAll("button")
                    .forEach(btn => btn.disabled = true);

                feedback.classList.remove("hidden");
                feedback.textContent = "Checking your answer...";

                try {

                    const response = await apiRequest(
                        "/ai/quiz/answer",
                        {
                            method: "POST",
                            body: JSON.stringify({
                                quizId: quizId,
                                questionIndex: index,
                                selectedOption: option.id
                            })
                        }
                    );

                    answered++;
                    score = response.score;


                    // Highlight correct and wrong options
                    optionsContainer.querySelectorAll("button")
                        .forEach(btn => {

                            if (btn.dataset.optionId === response.correctAnswer) {
                                btn.classList.add("correct-option");
                            }

                            if (
                                btn.dataset.optionId === option.id &&
                                !response.correct
                            ) {
                                btn.classList.add("wrong-option");
                            }
                        });


                    // Show feedback
                    feedback.innerHTML = "";

                    feedback.classList.remove(
                        "correct-feedback",
                        "wrong-feedback"
                    );

                    feedback.classList.add(
                        response.correct
                            ? "correct-feedback"
                            : "wrong-feedback"
                    );

                    const status = document.createElement("h4");

                    status.textContent = response.correct
                        ? "✓ Correct Answer!"
                        : "✗ Incorrect Answer!";

                    const answer = document.createElement("p");

                    answer.textContent =
                        "Correct Answer: " + response.correctAnswer;

                    const explanation = document.createElement("p");

                    explanation.textContent =
                        response.explanation || "No explanation available.";

                    feedback.append(status, answer, explanation);


                    // Update score
                    scoreDisplay.textContent =
                        `Answered: ${answered} / ${questions.length} | Score: ${score}`;


                    // Show final score
                    if (response.completed) {

                        const finalResult = document.createElement("div");
                        finalResult.className = "quiz-final-result";

                        const finalHeading = document.createElement("h3");
                        finalHeading.textContent = "🎉 Quiz Completed!";

                        const finalScore = document.createElement("p");

                        finalScore.textContent =
                            `Your Score: ${response.score} / ${response.totalQuestions}`;

                        finalResult.append(finalHeading, finalScore);
                        result.appendChild(finalResult);
                    }

                } catch (error) {

                    console.error("Quiz answer API error:", error);

                    // Allow retry
                    optionsContainer.dataset.answered = "false";

                    optionsContainer.querySelectorAll("button")
                        .forEach(btn => btn.disabled = false);

                    feedback.classList.remove(
                        "correct-feedback",
                        "wrong-feedback"
                    );

                    feedback.textContent =
                        error.message || "Unable to check answer. Please try again.";
                }

            });

            optionsContainer.appendChild(button);

        });


        card.append(
            title,
            questionText,
            optionsContainer,
            feedback
        );

        result.appendChild(card);

    });

}



/* =========================================
   DISPLAY USER NAME
========================================= */

let user = {};

try {
    user = JSON.parse(localStorage.getItem("user") || "{}");
} catch (error) {
    user = {};
}

const userName = document.getElementById("userName");

if (userName) {
    userName.textContent = user.name || "Student";
}


/* =========================================
   SIDEBAR NAVIGATION
========================================= */

document
    .querySelectorAll(".sidebar-btn[data-page]")
    .forEach(button => {

        button.addEventListener("click", function () {

            document
                .querySelectorAll(".sidebar-btn")
                .forEach(btn => {
                    btn.classList.remove("active");
                });

            this.classList.add("active");

            currentMode = this.dataset.page;

            changePage();

        });

    });


/* =========================================
   CHANGE DASHBOARD PAGE
========================================= */

function changePage() {

    quizOptions.classList.add("hidden");

    topicInput.style.display = "block";
    generateBtn.style.display = "inline-block";

    if (currentMode === "ask") {

        pageTitle.textContent = "Ask AI";

        pageSubtitle.textContent =
            "Ask any question related to your studies.";

        topicInput.placeholder =
            "Example: Explain inheritance in Java";

        generateBtn.textContent = "Ask AI";

        result.textContent =
            "Your AI response will appear here.";

    }

    else if (currentMode === "notes") {

        pageTitle.textContent = "Generate Notes";

        pageSubtitle.textContent =
            "Create simple and useful study notes.";

        topicInput.placeholder =
            "Example: Java Exception Handling";

        generateBtn.textContent = "Generate Notes";

        result.textContent =
            "Your generated notes will appear here.";

    }

    else if (currentMode === "quiz") {

        pageTitle.textContent = "Generate Quiz";

        pageSubtitle.textContent =
            "Create questions for practice.";

        topicInput.placeholder =
            "Example: DBMS";

        generateBtn.textContent = "Generate Quiz";

        quizOptions.classList.remove("hidden");

        result.textContent =
            "Your generated quiz will appear here.";

    }

    else if (currentMode === "history") {

        pageTitle.textContent = "Study History";

        pageSubtitle.textContent =
            "View your recent study activities.";

        topicInput.style.display = "none";
        generateBtn.style.display = "none";

        loadHistory();

    }

}


/* =========================================
   GENERATE AI CONTENT
========================================= */

generateBtn.addEventListener("click", async function () {

    const topic = topicInput.value.trim();
    const mode = currentMode;

    if (!topic) {
        result.textContent = "Please enter a topic.";
        return;
    }

    generateBtn.disabled = true;

    result.textContent = "AI is generating your response...";


    try {

        let endpoint = "/ai/explain";

        const body = {
            topic: topic
        };


        /* Select API Endpoint */

        if (mode === "notes") {

            endpoint = "/ai/notes";

        }

        else if (mode === "quiz") {

            endpoint = "/ai/quiz";

        }


        /* Send API Request */

        const data = await apiRequest(
            endpoint,
            {
                method: "POST",
                body: JSON.stringify(body)
            }
        );


        /* Display API Response */

        if (mode === "quiz") {

            // Render interactive quiz
            await renderQuiz(data);

        }

        else if (mode === "notes") {

            // Display formatted notes
            displayResponse(data.notes || data.answer);

        }

        else {

            // Display AI explanation
            displayResponse(data.answer);

        }


    } catch (error) {

        result.textContent =
            error.message || "Something went wrong. Please try again.";

    }

    finally {

        generateBtn.disabled = false;

    }

});


/* =========================================
   STUDY HISTORY
========================================= */

async function loadHistory() {

    result.textContent = "Loading history...";

    try {

        const history = await apiRequest("/history");

        if (!history || history.length === 0) {

            result.textContent =
                "No study history found.";

            return;

        }


        // Clear previous content
        result.innerHTML = "";


        history.forEach(item => {

            const historyItem = document.createElement("div");

            historyItem.classList.add("history-item");


            // History heading
            const heading = document.createElement("h3");

            heading.textContent =
                `[${item.type}] ${item.topic}`;


            // History content
            const content = document.createElement("div");

            content.classList.add("history-content");

            const html = marked.parse(
                String(item.content || "")
            );

            content.innerHTML =
                DOMPurify.sanitize(html);


            historyItem.appendChild(heading);
            historyItem.appendChild(content);

            result.appendChild(historyItem);

        });


    } catch (error) {

        result.textContent =
            error.message || "Unable to load study history.";

    }

}


/* =========================================
   LOGOUT
========================================= */

const logoutBtn = document.getElementById("logoutBtn");

if (logoutBtn) {

    logoutBtn.addEventListener("click", function () {

        localStorage.clear();

        window.location.href = "login.html";

    });

}


/* =========================================
   INITIALIZE DASHBOARD
========================================= */

changePage();
