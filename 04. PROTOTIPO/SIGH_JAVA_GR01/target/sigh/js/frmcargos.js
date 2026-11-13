// Filtro de la tabla y vista previa del código correlativo del cargo.
const filtro = document.getElementById("filtro");
if (filtro) {
    filtro.addEventListener("change", () => filtro.form.submit());
}

const departamento = document.getElementById("departamento");
const codigo = document.getElementById("codigo");
if (departamento && codigo && departamento.tagName === "SELECT") {
    const mostrar = () => {
        codigo.value = departamento.selectedOptions[0].dataset.siguiente || "";
    };
    departamento.addEventListener("change", mostrar);
}
