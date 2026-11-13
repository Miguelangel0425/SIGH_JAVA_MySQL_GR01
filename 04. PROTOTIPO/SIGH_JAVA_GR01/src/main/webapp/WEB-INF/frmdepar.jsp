<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Departamentos | SIGH</title>
    <link rel="stylesheet" href="${base}/css/departamentos.css">
</head>
<body>
    <main class="gestion">
        <header class="gestion-cabecera">
            <div>
                <p class="gestion-etiqueta">SIGH · SUBSISTEMA DE PERSONAL</p>
                <h1>Departamentos</h1>
                <p class="gestion-texto">Administra el catálogo de departamentos del hospital.</p>
            </div>
            <div class="gestion-acciones">
                <a class="enlace-personal" href="${base}/inicio">Inicio</a>
                <a class="enlace-personal" href="${base}/cargos">Cargos</a>
                <form action="${base}/salir" method="post">
                    <input type="hidden" name="token" value="${token}">
                    <button class="accion" type="submit">Cerrar sesión</button>
                </form>
            </div>
        </header>
        <div class="gestion-contenido">
            <section aria-labelledby="listado">
                <h2 id="listado">Departamentos registrados</h2>
                <div class="gestion-tabla">
                    <table>
                        <thead>
                            <tr><th scope="col">Código</th><th scope="col">Descripción</th><th scope="col">Acciones</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${departamentos}">
                                <tr>
                                    <th scope="row"><c:out value="${item.codigo}" /></th>
                                    <td><c:out value="${item.descripcion}" default="—" /></td>
                                    <td class="gestion-acciones">
                                        <a href="${base}/departamentos?modo=editar&amp;codigo=${item.codigo}#operacion">Editar</a>
                                        <a class="peligro" href="${base}/departamentos?modo=eliminar&amp;codigo=${item.codigo}#operacion">Eliminar</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty departamentos}">
                                <tr><td colspan="3">Aún no hay departamentos registrados.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="gestion-formulario" aria-labelledby="operacion">
                <c:choose>
                    <c:when test="${modo eq 'editar'}"><h2 id="operacion">Editar departamento</h2></c:when>
                    <c:when test="${modo eq 'eliminar'}"><h2 id="operacion">Eliminar departamento</h2></c:when>
                    <c:otherwise><h2 id="operacion">Nuevo departamento</h2></c:otherwise>
                </c:choose>
                <c:if test="${not empty error}">
                    <p class="alerta" role="alert"><c:out value="${error}" /></p>
                </c:if>
                <form action="${base}/departamentos" method="post">
                    <input type="hidden" name="token" value="${token}">
                    <input type="hidden" name="modo" value="${modo}">
                    <c:choose>
                        <c:when test="${modo eq 'eliminar'}">
                            <input type="hidden" name="codigo" value="${departamento.codigo}">
                            <p class="gestion-texto">¿Quieres eliminar el departamento <strong><c:out value="${departamento.codigo}" /></strong>?</p>
                            <button class="accion accion-peligro" type="submit">Confirmar eliminación</button>
                        </c:when>
                        <c:otherwise>
                            <div class="campo">
                                <label for="codigo">Código</label>
                                <c:choose>
                                    <c:when test="${modo eq 'editar'}">
                                        <input id="codigo" name="codigo" value="${departamento.codigo}" readonly>
                                    </c:when>
                                    <c:otherwise>
                                        <input id="codigo" name="codigo" value="<c:out value='${departamento.codigo}' />" maxlength="3" pattern="[A-Za-z0-9]{1,3}" placeholder="Ej. RAD" required>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="campo">
                                <label for="descripcion">Descripción <span>(opcional)</span></label>
                                <input id="descripcion" name="descripcion" value="<c:out value='${departamento.descripcion}' />" maxlength="50">
                            </div>
                            <button class="accion" type="submit">Guardar departamento</button>
                        </c:otherwise>
                    </c:choose>
                </form>
                <c:if test="${modo ne 'crear'}">
                    <a class="enlace-personal gestion-cancelar" href="${base}/departamentos">Cancelar</a>
                </c:if>
            </section>
        </div>
    </main>
</body>
</html>
