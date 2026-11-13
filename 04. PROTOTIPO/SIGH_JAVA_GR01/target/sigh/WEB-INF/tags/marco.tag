<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="titulo" required="true" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#211c45">
    <title>${titulo} | SIGH</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/acceso.css">
    <script src="${pageContext.request.contextPath}/js/acceso.js" defer></script>
</head>
<body>
    <main class="portal">
        <aside class="campus" aria-label="Edición inspirada en Monsters University">
            <img class="mascota" src="${pageContext.request.contextPath}/resources/sullivan.png" alt="Sullivan con bata médica y estetoscopio" width="545" height="817">
        </aside>
        <section class="panel" aria-labelledby="titulo">
            <div class="panel-contenido">
                <div class="marca">
                    <span class="escudo" aria-hidden="true">S</span>
                    <div><strong>SIGH</strong><span>Gestión hospitalaria</span></div>
                </div>
                <jsp:doBody />
            </div>
        </section>
    </main>
</body>
</html>
