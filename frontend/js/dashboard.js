if(!localStorage.getItem("token")){
    location.href="login.html";
}

let mode="explain";

const title =
    document.getElementById("title");

const subtitle =
    document.getElementById("subtitle");

const topic =
    document.getElementById("topic");

const generate =
    document.getElementById("generate");

const result =
    document.getElementById("result");

const quizOptions =
    document.getElementById("quizOptions");

const user =
    JSON.parse(
        localStorage.getItem("user") || "{}"
    );

document.getElementById("userMini")
.textContent =
    user.name || "Student";


/* =========================
   NAVIGATION
========================= */

document
.querySelectorAll(".nav[data-mode]")
.forEach(button => {

    button.onclick = () => {

        document
        .querySelectorAll(".nav")
        .forEach(item =>
            item.classList.remove("active")
        );

        button.classList.add("active");

        mode =
            button.dataset.mode;

        if(mode === "history"){
            loadHistory();
            return;
        }

        generate.style.display="block";
        topic.style.display="block";

        quizOptions.classList.toggle(
            "hidden",
            mode !== "quiz"
        );

        if(mode==="explain"){

            title.textContent="Ask AI";

            subtitle.textContent=
                "Ask any study question.";

            generate.textContent="Ask AI";
        }

        if(mode==="notes"){

            title.textContent=
                "Generate Notes";

            subtitle.textContent=
                "Create exam-oriented notes.";

            generate.textContent=
                "Generate Notes";
        }

        if(mode==="quiz"){

            title.textContent=
                "Generate Quiz";

            subtitle.textContent=
                "Create MCQs for practice.";

            generate.textContent=
                "Generate Quiz";
        }

        result.textContent=
            "Your AI response will appear here.";
    };
});


/* =========================
   GENERATE AI RESPONSE
========================= */

generate.onclick = async () => {

    if(!topic.value.trim()){
        return;
    }

    generate.disabled=true;

    result.textContent="Thinking...";

    try{

        let path="/ai/explain";

        let body={
            topic:topic.value
        };

        if(mode==="notes"){
            path="/ai/notes";
        }

        if(mode==="quiz"){

            path="/ai/quiz";

            body={
                topic:topic.value,

                difficulty:
                    document.getElementById(
                        "difficulty"
                    ).value,

                numberOfQuestions:
                    Number(
                        document.getElementById(
                            "count"
                        ).value
                    )
            };
        }


        const data =
            await api(
                path,
                {
                    method:"POST",
                    body:JSON.stringify(body)
                }
            );


        /* =========================
           FORMAT AI RESPONSE
        ========================= */

        const answer =
            data.answer ||
            data.notes ||
            data.quiz ||
            "AI returned an empty response.";


        /*
         * Convert Markdown from Gemini
         * into properly formatted HTML.
         */
        result.innerHTML =
            marked.parse(answer);


    }catch(error){

        result.textContent =
            error.message;

    }finally{

        generate.disabled=false;
    }
};


/* =========================
   STUDY HISTORY
========================= */

async function loadHistory(){

    title.textContent=
        "Study History";

    subtitle.textContent=
        "Your latest AI study activity.";

    generate.style.display="none";

    topic.style.display="none";

    quizOptions.classList.add("hidden");

    result.textContent="Loading...";

    try{

        const items =
            await api("/history");

        if(!items.length){

            result.textContent=
                "No study history yet.";

            return;
        }


        /*
         * Format each history item
         * using Markdown.
         */
        result.innerHTML =
            items.map(item => {

                return `
                    <div class="history-item">

                        <h3>
                            ${escapeHtml(item.type)}
                        </h3>

                        <h4>
                            ${escapeHtml(item.topic)}
                        </h4>

                        <div class="history-content">
                            ${marked.parse(item.content || "")}
                        </div>

                    </div>
                `;

            }).join("");


    }catch(error){

        result.textContent=
            error.message;
    }
}


/* =========================
   HTML ESCAPE
========================= */

function escapeHtml(value){

    return String(value || "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


/* =========================
   LOGOUT
========================= */

document.getElementById("logout")
.onclick=()=>{

    localStorage.clear();

    location.href="login.html";
};