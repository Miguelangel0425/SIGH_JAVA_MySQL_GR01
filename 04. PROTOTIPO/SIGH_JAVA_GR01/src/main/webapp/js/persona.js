// Pestañas (la cabecera con código, apellidos, nombres y foto queda fuera y no se oculta),
// vista previa de la foto y confirmación al borrar.
const formulario = document.getElementById("frmpersona");
const campoPestana = document.getElementById("tab");
const botones = Array.from(document.querySelectorAll(".pestanas [role=tab]"));
const paneles = botones.map((b) => document.getElementById(b.dataset.panel));

function activar(id, enfocar) {
    botones.forEach((b) => {
        const activo = b.dataset.panel === id;
        b.setAttribute("aria-selected", activo ? "true" : "false");
        b.tabIndex = activo ? 0 : -1;
        if (activo && enfocar) {
            b.focus();
        }
    });
    paneles.forEach((p) => { p.hidden = p.id !== id; });
    campoPestana.value = id;
}

if (botones.length) {
    botones[0].parentElement.hidden = false;
    botones.forEach((b, i) => {
        b.addEventListener("click", () => activar(b.dataset.panel, false));
        b.addEventListener("keydown", (e) => {
            const salto = e.key === "ArrowRight" ? 1 : e.key === "ArrowLeft" ? -1 : 0;
            if (salto) {
                e.preventDefault();
                activar(botones[(i + salto + botones.length) % botones.length].dataset.panel, true);
            }
        });
    });
    const inicial = botones.find((b) => b.dataset.panel === campoPestana.value) || botones[0];
    activar(inicial.dataset.panel, false);

    // Si un campo obligatorio vacío está en otra pestaña, se abre esa pestaña para mostrar el aviso.
    formulario.addEventListener("invalid", (e) => {
        const panel = e.target.closest(".panel-pestana");
        if (panel && panel.hidden) {
            activar(panel.id, false);
        }
    }, true);

    // El enlace de navegación conserva la pestaña actual.
    document.querySelectorAll(".ficha-nav a, .botonera a").forEach((a) => {
        a.addEventListener("click", () => {
            const url = new URL(a.href);
            url.searchParams.set("tab", campoPestana.value);
            a.href = url.toString();
        });
    });
}

const foto = document.getElementById("foto");
const vista = document.getElementById("vista-foto");
if (foto && vista) {
    foto.addEventListener("change", () => {
        const archivo = foto.files[0];
        if (!archivo) {
            return;
        }
        if (archivo.size > 2 * 1024 * 1024) {
            alert("La foto no puede superar 2 MB. Elige una más liviana.");
            foto.value = "";
            return;
        }
        vista.src = URL.createObjectURL(archivo);
    });
}

document.querySelectorAll("[data-confirmar]").forEach((b) => {
    b.addEventListener("click", (e) => {
        if (!confirm(b.dataset.confirmar)) {
            e.preventDefault();
        }
    });
});

// Documento: cédula o pasaporte (uno solo). Al cambiar el tipo se ajusta la etiqueta y el formato.
const tipoDocumento = document.getElementById("tipoDocumento");
const documento = document.getElementById("documento");
const etiquetaDocumento = document.getElementById("etiqueta-documento");

function ajustarDocumento(limpiar) {
    const cedula = tipoDocumento.value === "CEDULA";
    etiquetaDocumento.textContent = cedula ? "Número de cédula" : "Número de pasaporte";
    documento.maxLength = cedula ? 10 : 15;
    documento.pattern = cedula ? "[0-9]{10}" : "[A-Za-z0-9]{1,15}";
    documento.inputMode = cedula ? "numeric" : "text";
    documento.placeholder = cedula ? "10 dígitos" : "Hasta 15 letras o números";
    if (limpiar) {
        documento.value = "";
    }
}

if (tipoDocumento && documento && etiquetaDocumento) {
    ajustarDocumento(false);
    tipoDocumento.addEventListener("change", () => ajustarDocumento(true));
}

// Estado inicial: sin persona seleccionada, todo bloqueado hasta pulsar Nuevo o elegir una de la lista.
if (formulario.dataset.bloqueado === "true") {
    formulario.querySelectorAll("input:not([type=hidden]), select, button[name=accion]").forEach((el) => {
        el.disabled = true;
    });
}