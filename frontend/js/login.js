document
.getElementById("loginForm")
.addEventListener("submit", async function(event){

    event.preventDefault();

    const message =
        document.getElementById("message");

    try {

        const data = await api(
            "/auth/login",
            {
                method:"POST",
                body:JSON.stringify({
                    email:
                        document.getElementById("email").value,
                    password:
                        document.getElementById("password").value
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

        location.href = "dashboard.html";

    } catch(error) {

        message.textContent =
            error.message;
    }
});
