const signupForm =
    document.getElementById("signupForm");


signupForm.addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();


        const name =
            document.getElementById("name").value;

        const email =
            document.getElementById("email").value;

        const password =
            document.getElementById("password").value;

        const confirmPassword =
            document.getElementById(
                "confirmPassword"
            ).value;


        const message =
            document.getElementById("message");


        if (password !== confirmPassword) {

            message.textContent =
                "Passwords do not match.";

            return;
        }


        message.textContent =
            "Creating account...";


        try {

            const data =
                await apiRequest(
                    "/auth/signup",
                    {
                        method: "POST",

                        body: JSON.stringify({
                            name: name,
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