<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sigh" tagdir="/WEB-INF/tags" %>
<sigh:marco titulo="Iniciar sesión">
    <h1 id="titulo">Iniciar sesión</h1>
    <p class="introduccion">Ingresa tus credenciales para acceder al Sistema Integral de Gestión Hospitalaria.</p>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <input type="hidden" name="token" value="${requestScope.token}">
        <c:if test="${not empty requestScope.error}">
            <p class="alerta" role="alert"><c:out value="${requestScope.error}" /></p>
        </c:if>
        <div class="campo">
            <label for="nombre">Usuario</label>
            <input id="nombre" name="nombre" value="<c:out value='${requestScope.nombre}' />" placeholder="Escribe tu usuario" autocomplete="username" maxlength="64" required>
        </div>
        <div class="campo">
            <label for="clave">Contraseña</label>
            <div class="entrada-clave">
                <input id="clave" name="clave" type="password" placeholder="Escribe tu contraseña" autocomplete="current-password" maxlength="128" required>
                <button type="button" class="ver-clave" aria-controls="clave" hidden>Mostrar</button>
            </div>
        </div>
        <button class="accion accion-ingresar" type="submit">Ingresar <span aria-hidden="true">→</span></button>
    </form>
    <footer class="pie">
        <span>SIGH · GRUPO 01</span>
        <span>GESTIÓN HOSPITALARIA</span>
    </footer>
</sigh:marco>
