document.addEventListener("DOMContentLoaded", () => {
  // URL base da sua API
  const API_URL = "http://10.6.53.199:8089/api";

  // Elementos do DOM
  const senhaAtualEl = document.getElementById("senha-atual");
  const guicheAtualEl = document.getElementById("guiche-atual");
  const historicoListaEl = document.getElementById("historico-lista");
  const relogioEl = document.getElementById("relogio");
  const notificationSound = document.getElementById("notification-sound");
  const chamadaPrincipalEl = document.getElementById("chamada-principal");
  const ativarAudioBtn = document.getElementById("ativar-audio");

  let ultimaSenhaExibida = null;
  let audioAtivado = false;
  let vozBrasileira = null;

  // --- LÓGICA DA SÍNTESE DE VOZ ---

  // Carrega e seleciona a voz em Português do Brasil
  // Função que carrega e seleciona a melhor voz em Português do Brasil
  function carregarVozes() {
    const vozes = window.speechSynthesis.getVoices();
    if (vozes.length === 0) {
      console.warn("Nenhuma voz de síntese encontrada.");
      return;
    }

    const vozesBrasileiras = vozes.filter((voz) => voz.lang === "pt-BR");
    if (vozesBrasileiras.length === 0) {
      console.warn("Nenhuma voz em Português do Brasil encontrada.");
      return;
    }

    // 1. Tenta encontrar uma voz com nome claramente feminino
    const nomesFemininos = [
      "luciana",
      "camila",
      "maria",
      "francisca",
      "fernanda",
      "female",
      "feminino",
    ];
    let vozEncontrada = vozesBrasileiras.find((voz) =>
      nomesFemininos.some((nome) => voz.name.toLowerCase().includes(nome)),
    );

    // 2. Se não encontrar, tenta pegar a voz padrão do navegador para pt-BR
    if (!vozEncontrada) {
      vozEncontrada = vozesBrasileiras.find((voz) => voz.default);
    }

    // 3. Se ainda não encontrou, pega a primeira voz pt-BR da lista
    if (!vozEncontrada) {
      vozEncontrada = vozesBrasileiras[0];
    }

    vozBrasileira = vozEncontrada;

    // Dica: Abra o console (F12) para ver qual voz foi selecionada!
    console.log(
      "Voz selecionada:",
      vozBrasileira ? vozBrasileira.name : "Nenhuma voz pt-BR disponível.",
    );
  }

  // Variáveis de controle para as repetições
  let repeticaoTimeout = null;
  let chamadaAtualTexto = "";

  // Função que converte texto em fala (com repetição e interrupção inteligente)
  function falarChamada(texto, contagem = 1) {
    if (!audioAtivado || !("speechSynthesis" in window)) {
      return; // Só fala se o áudio estiver ativado e o navegador suportar
    }

    // Se for a primeira chamada dessa senha (nova chamada ou rechamada do botão)
    if (contagem === 1) {
      // Limpa o temporizador da senha anterior para não encavalar
      clearTimeout(repeticaoTimeout);
      // Faz a TV "calar a boca" imediatamente se estiver a falar a senha velha
      window.speechSynthesis.cancel();
      // Grava qual é a senha "oficial" do momento
      chamadaAtualTexto = texto;
    } else {
      // Se for a repetição 2 ou 3, mas a senha "oficial" mudou no meio do caminho,
      // significa que o atendente chamou outra. Então abortamos essa repetição.
      if (texto !== chamadaAtualTexto) return;
    }

    const utterance = new SpeechSynthesisUtterance(texto);
    utterance.lang = "pt-BR";
    utterance.rate = 0.9; // Velocidade da fala (1 é o padrão)

    if (vozBrasileira) {
      utterance.voice = vozBrasileira;
    }

    // O evento 'onend' é disparado automaticamente assim que a voz TERMINA de falar a frase
    utterance.onend = () => {
      // Se já não tiver chegado outra senha E a contagem for menor que 3 (limite)
      if (texto === chamadaAtualTexto && contagem < 3) {
        // Agenda a próxima fala para daqui a 5 segundos (5000 milissegundos)
        repeticaoTimeout = setTimeout(() => {
          falarChamada(texto, contagem + 1);
        }, 5000);
      }
    };

    window.speechSynthesis.speak(utterance);
  }

  // O navegador dispara este evento quando as vozes estão prontas
  window.speechSynthesis.onvoiceschanged = carregarVozes;

  // --- LÓGICA PRINCIPAL DO PAINEL ---

  let senhasExibidas = new Set();
  let ultimaHoraChamada = null;
  async function atualizarPainel() {
    try {
      const response = await fetch(`${API_URL}/painel/ultimas-chamadas`);
      if (!response.ok) return;

      const ultimasChamadas = await response.json();
      if (ultimasChamadas.length === 0) return;

      const senhaMaisRecente = ultimasChamadas[0]; // A senha do topo

      // Se o horário de chamada da senha do topo for diferente do último registrado, é uma chamada nova (ou rechamada)!
      if (senhaMaisRecente.dataHoraChamada !== ultimaHoraChamada) {
        const prefixo =
          senhaMaisRecente.tipoSenha === "PREFERENCIAL" ? "P" : "N";
        const numeroFormatado = prefixo + senhaMaisRecente.numero;
        const guicheFormatado = String(
          senhaMaisRecente.guicheAtendimento,
        ).padStart(2, "0");

        senhaAtualEl.textContent = numeroFormatado;
        guicheAtualEl.textContent = guicheFormatado;

        notificationSound
          .play()
          .catch((e) => console.error("Erro ao tocar som:", e));

        const textoParaFalar = `Senha ${prefixo}, ${senhaMaisRecente.numero}. Dirija-se ao guichê ${parseInt(guicheFormatado, 10)}.`;
        falarChamada(textoParaFalar);

        chamadaPrincipalEl.classList.add("flash");
        setTimeout(() => chamadaPrincipalEl.classList.remove("flash"), 500);

        // Grava o novo horário para não repetir o som no próximo segundo
        ultimaHoraChamada = senhaMaisRecente.dataHoraChamada;
      }

      // Atualiza o histórico lateral
      historicoListaEl.innerHTML = "";
      ultimasChamadas.slice(1, 5).forEach((senha) => {
        const p = senha.tipoSenha === "PREFERENCIAL" ? "P" : "N";
        const n = p + senha.numero;
        const g = String(senha.guicheAtendimento).padStart(2, "0");
        const listItem = document.createElement("li");
        listItem.innerHTML = `<span>SENHA <strong>${n}</strong></span><span>GUICHÊ <strong>${g}</strong></span>`;
        historicoListaEl.appendChild(listItem);
      });
    } catch (error) {
      console.error("Erro na atualização do painel:", error);
    }
  }

  // Função para atualizar o relógio
  function atualizarRelogio() {
    const agora = new Date();
    const data = agora.toLocaleDateString("pt-BR");
    const hora = agora.toLocaleTimeString("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    });
    relogioEl.textContent = `${data} - ${hora}`;
  }

  // --- INICIALIZAÇÃO ---

  // Evento para o botão de ativar áudio
  ativarAudioBtn.addEventListener("click", () => {
    audioAtivado = true;
    ativarAudioBtn.textContent = "✔️ Som Ativado";
    ativarAudioBtn.style.backgroundColor = "#28a745";

    // Faz uma primeira "fala" silenciosa para ativar o motor de voz
    const utterance = new SpeechSynthesisUtterance("Áudio habilitado");
    utterance.volume = 0;
    window.speechSynthesis.speak(utterance);
  });

  setInterval(atualizarPainel, 1000);
  setInterval(atualizarRelogio, 100);

  atualizarPainel();
  atualizarRelogio();
});
