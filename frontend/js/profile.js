if (!localStorage.getItem("token")) {

    window.location.href =
        "login.html";

}


const avatar =
    document.getElementById(
        "profileAvatar"
    );

const pictureInput =
    document.getElementById(
        "profilePicture"
    );

const message =
    document.getElementById(
        "profileMessage"
    );



/* Display User */

function displayUser(user) {

    document.getElementById(
        "profileName"
    ).value =
        user.name || "";


    document.getElementById(
        "profileEmail"
    ).value =
        user.email || "";


    /*
        If profile picture exists,
        display it.

        Otherwise display the
        default blank avatar.
    */

    if (user.profilePicture) {

        avatar.innerHTML =
            `<img
                src="http://localhost:8080${user.profilePicture}"
                alt="Profile Picture"
            >`;

    } else {

        avatar.innerHTML =
            "👤";

    }


    localStorage.setItem(
        "user",
        JSON.stringify(user)
    );

}



/* Load Profile */

async function loadProfile() {

    try {

        const user =
            await apiRequest(
                "/users/me"
            );

        displayUser(user);

    } catch (error) {

        message.textContent =
            error.message;

    }

}


loadProfile();



/* Upload Button */

document
.getElementById("uploadBtn")
.addEventListener(
    "click",
    function() {

        pictureInput.click();

    }
);



/* Upload Picture */

pictureInput.addEventListener(
    "change",
    async function() {

        if (!pictureInput.files.length) {

            return;

        }


        const formData =
            new FormData();


        formData.append(
            "file",
            pictureInput.files[0]
        );


        message.textContent =
            "Uploading picture...";


        try {

            const user =
                await apiRequest(
                    "/users/profile-picture",
                    {
                        method: "POST",
                        body: formData
                    }
                );


            displayUser(user);


            message.textContent =
                "Profile picture uploaded.";

        } catch (error) {

            message.textContent =
                error.message;

        }

    }
);



/* Remove Picture */

document
.getElementById("removeBtn")
.addEventListener(
    "click",
    async function() {

        try {

            const user =
                await apiRequest(
                    "/users/profile-picture",
                    {
                        method: "DELETE"
                    }
                );


            displayUser(user);


            message.textContent =
                "Profile picture removed.";

        } catch (error) {

            message.textContent =
                error.message;

        }

    }
);



/* Save Profile */

document
.getElementById("saveProfileBtn")
.addEventListener(
    "click",
    async function() {

        const name =
            document.getElementById(
                "profileName"
            ).value;


        try {

            const user =
                await apiRequest(
                    "/users/me",
                    {
                        method: "PUT",

                        body:
                            JSON.stringify({
                                name: name
                            })
                    }
                );


            displayUser(user);


            message.textContent =
                "Profile updated successfully.";

        } catch (error) {

            message.textContent =
                error.message;

        }

    }
);