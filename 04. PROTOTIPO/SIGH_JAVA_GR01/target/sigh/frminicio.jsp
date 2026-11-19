<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sigh" tagdir="/WEB-INF/tags" %>
<sigh:marco titulo="Sesión iniciada">
    <p class="estado">✓ Sesión iniciada</p>
    <h1 id="titulo">Hola, <c:out value="${requestScope.nombre}" />.</h1>
    <p class="introduccion">Ingresaste al prototipo del Sistema Integral de Gestión Hospitalaria.</p>
    <p><a class="accion" href="${pageContext.request.contextPath}/departamentos">Departamentos <span aria-hidden="true">→</span></a>
       <a class="accion" href="${pageContext.request.contextPath}/cargos">Cargos <span aria-hidden="true">→</span></a>
       <a class="accion" href="${pageContext.request.contextPath}/persona">Personas <span aria-hidden="true">→</span></a></p>
    <form action="${pageContext.request.contextPath}/salir" method="post">
        <input type="hidden" name="token" value="${requestScope.token}">
        <button class="accion" type="submit">Cerrar sesión <span aria-hidden="true">→</span></button>
    </form>
</sigh:marco>
