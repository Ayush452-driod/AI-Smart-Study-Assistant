if(!localStorage.getItem("token")){
    location.href="login.html";
}

const avatar =
    document.getElementById("avatar");

const picture =
    document.getElementById("picture");

const message =
    document.getElementById("message");

function displayUser(user){

    document.getElementById("name")
        .value=user.name || "";

    document.getElementById("email")
        .value=user.email || "";

    if(user.profilePicture){

        avatar.innerHTML =
            `<img src="http://localhost:8080${user.profilePicture}"
             alt="Profile picture">`;

    }else{

        // Blank/default picture.
        avatar.textContent="👤";
    }

    localStorage.setItem(
        "user",
        JSON.stringify(user)
    );
}

(async function(){

    try{

        const user =
            await api("/users/me");

        displayUser(user);

    }catch(error){

        message.textContent=
            error.message;
    }

})();

document.getElementById("save")
.onclick=async function(){

    try{

        const user =
            await api(
                "/users/me",
                {
                    method:"PUT",
                    body:JSON.stringify({
                        name:
                            document.getElementById(
                                "name"
                            ).value
                    })
                }
            );

        displayUser(user);

        message.textContent=
            "Profile updated.";

    }catch(error){

        message.textContent=
            error.message;
    }
};

document.getElementById("uploadBtn")
.onclick=()=>{
    picture.click();
};

picture.onchange=async function(){

    if(!picture.files[0]){
        return;
    }

    const formData =
        new FormData();

    formData.append(
        "file",
        picture.files[0]
    );

    try{

        const user =
            await api(
                "/users/profile-picture",
                {
                    method:"POST",
                    body:formData
                }
            );

        displayUser(user);

        message.textContent=
            "Profile picture uploaded.";

    }catch(error){

        message.textContent=
            error.message;
    }
};

document.getElementById("removeBtn")
.onclick=async function(){

    try{

        const user =
            await api(
                "/users/profile-picture",
                {
                    method:"DELETE"
                }
            );

        displayUser(user);

        message.textContent=
            "Profile picture removed.";

    }catch(error){

        message.textContent=
            error.message;
    }
};
