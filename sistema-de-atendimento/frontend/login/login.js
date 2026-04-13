document.getElementById("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const errorDiv = document.getElementById("login-error");

  try {
    const response = await fetch("http://10.6.53.199:8089/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) throw new Error("Usuario ou senha invalidos.");

    // 🔑 Salva o token no armazenamento local do navegador
    const tokenObject = await response.json();
    localStorage.setItem("authToken", tokenObject.token);

    window.location.href = "../atendente/index.html";
  } catch (error) {
    errorDiv.textContent = error.message;
  }
});
