document.addEventListener("DOMContentLoaded", () => {
  // URL base da sua API. Altere a porta se necessário.
  const API_URL = "http://10.6.53.199:8089/api";

  // Elementos da Área do Cliente
  const btnNormal = document.getElementById("btn-normal");
  const btnPreferencial = document.getElementById("btn-preferencial");
  const senhaGeradaDiv = document.getElementById("senha-gerada");

  // Elementos da Área do Atendente
  const btnChamar = document.getElementById("btn-chamar");
  const guicheInput = document.getElementById("guiche-input");
  const senhaChamadaDiv = document.getElementById("senha-chamada");

  // Elementos do Painel
  const painelSenha = document.getElementById("painel-senha");
  const painelGuiche = document.getElementById("painel-guiche");

  const historicoList = document.getElementById("historico-chamadas");

  const btnRechamar = document.getElementById("btn-rechamar");
  const rechamarNumeroInput = document.getElementById("rechamar-numero-input");
  const rechamarInfoDiv = document.getElementById("rechamar-info");

  async function atualizarHistorico() {
    try {
      const response = await fetch(`${API_URL}/painel/ultimas-chamadas`);
      if (!response.ok) return;

      const ultimasChamadas = await response.json();
      historicoList.innerHTML = "";

      // Atualiza o display grande com a chamada mais recente de todas (se existir)
      if (ultimasChamadas.length > 0) {
        const maisRecente = ultimasChamadas[0];
        const p = maisRecente.tipoSenha === "PREFERENCIAL" ? "P" : "N";
        painelSenha.textContent = p + maisRecente.numero;
        painelGuiche.textContent = maisRecente.guicheAtendimento;
      }

      ultimasChamadas.forEach((senha) => {
        const listItem = document.createElement("li");
        let sufixo = senha.tipoSenha === "PREFERENCIAL" ? "P" : "N";
        listItem.innerHTML = `
                <span>Senha <strong>${sufixo + senha.numero}</strong></span>
                <span>Guichê <strong>${senha.guicheAtendimento}</strong></span>
            `;
        historicoList.appendChild(listItem);
      });
    } catch (error) {
      console.error("Erro ao atualizar histórico:", error);
    }
  }

  // --- LÓGICA DO CLIENTE ---

  async function gerarSenha(tipo) {
    const token = localStorage.getItem("authToken");
    if (!token) {
      alert("Token não encontrado. Faça login novamente.");
      window.location.href = "../login/login.html"; // Ajuste o caminho se necessário
      return;
    }

    try {
      // URL CORRIGIDA: Aponta para o novo endpoint no AtendenteController
      const response = await fetch(
        `${API_URL}/atendentes/senhas?tipo=${tipo}`,
        {
          method: "POST",
          headers: { Authorization: "Bearer " + token },
        },
      );

      if (!response.ok) {
        throw new Error("Erro ao gerar senha.");
      }

      const data = await response.json();
      // A lógica de exibição permanece a mesma
      let sufixo = data.tipoSenha === "PREFERENCIAL" ? "P" : "N";
      senhaGeradaDiv.innerHTML = `Senha Gerada: <strong>${sufixo}${data.numero}</strong>`;
    } catch (error) {
      senhaGeradaDiv.textContent = "Falha na comunicação com o servidor.";
      console.error(error);
    }
  }

  btnNormal.addEventListener("click", () => gerarSenha("NORMAL"));
  btnPreferencial.addEventListener("click", () => gerarSenha("PREFERENCIAL"));

  // --- LÓGICA DO ATENDENTE ---

  // --- CHAMA A FUNÇÃO DE ATUALIZAÇÃO QUANDO A PÁGINA CARREGA PELA PRIMEIRA VEZ ---
  atualizarHistorico();

  async function chamarSenha() {
    const guiche = guicheInput.value.trim();
    if (!guiche) {
      alert("Por favor, informe o número do guichê.");
      return;
    }

    // 🔑 Recupera o token salvo no login
    const token = localStorage.getItem("authToken");

    if (!token) {
      alert("Token não encontrado. Faça login novamente.");
      window.location.href = "../login/login.html";
      return;
    }

    try {
      const response = await fetch(
        `${API_URL}/atendentes/chamar?guiche=${guiche}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token,
          },
        },
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Não há senhas para chamar.");
      }

      const data = await response.json();
      let sufixo = "";
      if (data.tipoSenha === "PREFERENCIAL") sufixo = "P";
      else sufixo = "N";
      const mensagem = `Chamando senha <strong>${sufixo + data.numero}</strong> no guichê <strong>${data.guicheAtendimento}</strong>`;

      // Atualiza a área do atendente
      senhaChamadaDiv.innerHTML = mensagem;

      // Atualiza o painel principal
      painelSenha.textContent = sufixo + data.numero;
      painelGuiche.textContent = data.guicheAtendimento;
      await atualizarHistorico();
    } catch (error) {
      senhaChamadaDiv.textContent = error.message;
      console.error(error);
    }
  }

  async function rechamarSenha() {
    const inputCompleto = rechamarNumeroInput.value.trim().toUpperCase(); // ex: "P12"
    const guiche = guicheInput.value.trim();

    const token = localStorage.getItem("authToken");

    if (!token) {
      alert("Token não encontrado. Faça login novamente.");
      window.location.href = "../login/login.html";
      return;
    }

    if (!inputCompleto || !guiche) {
      alert("Informe a senha (ex: P12) e o seu guichê.");
      return;
    }

    // Separa o tipo do número
    const tipoChar = inputCompleto.charAt(0); // 'P' ou 'N'
    const numero = inputCompleto.substring(1); // '12'

    if ((tipoChar !== "P" && tipoChar !== "N") || isNaN(numero)) {
      alert(
        "Formato de senha inválido. Use P ou N seguido do número (ex: P12).",
      );
      return;
    }

    const tipo = tipoChar === "P" ? "PREFERENCIAL" : "NORMAL";

    try {
      const response = await fetch(
        `${API_URL}/atendentes/senhas/rechamar?numero=${numero}&tipo=${tipo}&guiche=${guiche}`,
        {
          method: "PUT",
          headers: { Authorization: "Bearer " + token },
        },
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Não foi possível rechamar a senha.");
      }

      const data = await response.json();
      rechamarInfoDiv.textContent = `Senha #${tipoChar}${data.numero} rechamada com sucesso no guichê ${data.guicheAtendimento}.`;
      rechamarNumeroInput.value = ""; // Limpa o campo de input

      // --- ADICIONE ESTA LINHA ---
      // Força a atualização do histórico na TELA DO ATENDENTE imediatamente.
      await atualizarHistorico();
    } catch (error) {
      rechamarInfoDiv.textContent = error.message;
      console.error(error);
    }
  }

  document.getElementById("btn-sair").addEventListener("click", () => {
    localStorage.removeItem("authToken"); // Apaga o token
    window.location.href = "../login/login.html"; // Volta pro login
  });

  btnChamar.addEventListener("click", chamarSenha);
  btnRechamar.addEventListener("click", rechamarSenha);
});
