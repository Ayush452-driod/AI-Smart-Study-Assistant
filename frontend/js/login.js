const loginForm =
    document.getElementById("loginForm");


loginForm.addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();


        const email =
            document.getElementById("email").value;

        const password =
            document.getElementById("password").value;


        const message =
            document.getElementById("message");


        message.textContent =
            "Logging in...";


        try {

            const data =
                await apiRequest(
                    "/auth/login",
                    {
                        method: "POST",

                        body: JSON.stringify({
                            email: email,
                            password: password
                        })
                    }
                );


            localStorage.setItem(
                "token",
                data.token
            );


            localStorage.setItem(
                "user",
                JSON.stringify(data.user)
            );


            window.location.href =
                "dashboard.html";


        } catch (error) {

            message.textContent =
                error.message;

        }

    }
);