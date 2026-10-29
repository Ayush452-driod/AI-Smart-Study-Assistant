if (!localStorage.getItem("token")) {

    window.location.href =
        "login.html";

}


let currentMode = "ask";


const pageTitle =
    document.getElementById("pageTitle");

const pageSubtitle =
    document.getElementById("pageSubtitle");

const topicInput =
    document.getElementById("topicInput");

const generateBtn =
    document.getElementById("generateBtn");

const result =
    document.getElementById("result");

const quizOptions =
    document.getElementById("quizOptions");



/* Display User Name */

const user =
    JSON.parse(
        localStorage.getItem("user") || "{}"
    );


document.getElementById(
    "userName"
).textContent =
    user.name || "Student";



/* Sidebar */

document
.querySelectorAll(".sidebar-btn[data-page]")
.forEach(button => {

    button.addEventListener(
        "click",
        function() {

            document
            .querySelectorAll(
                ".sidebar-btn"
            )
            .forEach(btn =>
                btn.classList.remove(
                    "active"
                )
            );


            this.classList.add("active");


            currentMode =
                this.dataset.page;


            changePage();

        }
    );

});



function changePage() {

    quizOptions.classList.add(
        "hidden"
    );


    topicInput.style.display =
        "block";

    generateBtn.style.display =
        "inline-block";


    if (currentMode === "ask") {

        pageTitle.textContent =
            "Ask AI";

        pageSubtitle.textContent =
            "Ask any question related to your studies.";

        topicInput.placeholder =
            "Example: Explain inheritance in Java";

        generateBtn.textContent =
            "Ask AI";

    }


    else if (currentMode === "notes") {

        pageTitle.textContent =
            "Generate Notes";

        pageSubtitle.textContent =
            "Create simple and useful study notes.";

        topicInput.placeholder =
            "Example: Java Exception Handling";

        generateBtn.textContent =
            "Generate Notes";

    }


    else if (currentMode === "quiz") {

        pageTitle.textContent =
            "Generate Quiz";

        pageSubtitle.textContent =
            "Create questions for practice.";

        topicInput.placeholder =
            "Example: DBMS";

        generateBtn.textContent =
            "Generate Quiz";

        quizOptions.classList.remove(
            "hidden"
        );

    }


    else if (currentMode === "history") {

        pageTitle.textContent =
            "Study History";

        pageSubtitle.textContent =
            "View your recent study activities.";

        topicInput.style.display =
            "none";

        generateBtn.style.display =
            "none";

        loadHistory();

    }

}



/* Generate AI Content */

generateBtn.addEventListener(
    "click",
    async function() {

        const topic =
            topicInput.value.trim();


        if (!topic) {

            result.textContent =
                "Please enter a topic.";

            return;

        }


        generateBtn.disabled =
            true;

        result.textContent =
            "AI is generating your response...";


        try {

            let endpoint =
                "/ai/explain";

            let body = {
                topic: topic
            };


            if (currentMode === "notes") {

                endpoint =
                    "/ai/notes";

            }


            if (currentMode === "quiz") {

                endpoint =
                    "/ai/quiz";


                body = {

                    topic: topic,

                    difficulty:
                        document.getElementById(
                            "difficulty"
                        ).value,

                    numberOfQuestions:
                        Number(
                            document.getElementById(
                                "questionCount"
                            ).value
                        )

                };

            }


            const data =
                await apiRequest(
                    endpoint,
                    {
                        method: "POST",

                        body:
                            JSON.stringify(body)
                    }
                );


            result.textContent =
                data.answer ||
                data.notes ||
                data.quiz;


        } catch (error) {

            result.textContent =
                error.message;

        }


        finally {

            generateBtn.disabled =
                false;

        }

    }
);



/* Study History */

async function loadHistory() {

    result.textContent =
        "Loading history...";


    try {

        const history =
            await apiRequest(
                "/history"
            );


        if (!history ||
            history.length === 0) {

            result.textContent =
                "No study history found.";

            return;

        }


        result.textContent =
            history.map(
                item => {

                    return (
                        "[" +
                        item.type +
                        "] " +
                        item.topic +
                        "\n\n" +
                        item.content +
                        "\n\n--------------------\n\n"
                    );

                }
            ).join("");


    } catch (error) {

        result.textContent =
            error.message;

    }

}



/* Logout */

document
.getElementById("logoutBtn")
.addEventListener(
    "click",
    function() {

        localStorage.clear();

        window.location.href =
            "login.html";

    }
);