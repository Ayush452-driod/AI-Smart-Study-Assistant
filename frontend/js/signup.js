document
.getElementById("signupForm")
.addEventListener("submit", async function(event){

    event.preventDefault();

    const message =
        document.getElementById("message");

    const password =
        document.getElementById("password").value;

    const confirmPassword =
        document.getElementById("confirmPassword").value;

    if(password !== confirmPassword){

        message.textContent =
            "Passwords do not match.";

        return;
    }

    try {

        const data = await api(
            "/auth/signup",
            {
                method:"POST",
                body:JSON.stringify({
                    name:
                        document.getElementById("name").value,
                    email:
                        document.getElementById("email").value,
                    password
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

        location.href =
            "dashboard.html";

    } catch(error) {

        message.textContent =
            error.message;
    }
});
