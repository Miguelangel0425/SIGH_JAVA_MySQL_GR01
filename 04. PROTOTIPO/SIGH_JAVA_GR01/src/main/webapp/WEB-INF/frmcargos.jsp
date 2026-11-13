<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cargos | SIGH</title>
    <link rel="stylesheet" href="${base}/css/departamentos.css">
</head>
<body>
    <main class="gestion">
        <header class="gestion-cabecera">
            <div>
                <p class="gestion-etiqueta">SIGH · SUBSISTEMA DE PERSONAL</p>
                <h1>Cargos</h1>
                <p class="gestion-texto">Administra los cargos del hospital y su departamento.</p>
            </div>
            <div class="gestion-acciones">
                <a class="enlace-personal" href="${base}/inicio">Inicio</a>
                <a class="enlace-personal" href="${base}/departamentos">Departamentos</a>
                <form action="${base}/salir" method="post">
                    <input type="hidden" name="token" value="${token}">
                    <button class="accion" type="submit">Cerrar sesión</button>
                </form>
            </div>
        </header>
        <div class="gestion-contenido">
            <section aria-labelledby="listado">
                <h2 id="listado">Cargos registrados</h2>
                <form class="gestion-filtro" action="${base}/cargos" method="get">
                    <div class="campo">
                        <label for="filtro">Filtrar por departamento</label>
                        <select id="filtro" name="filtro">
                            <option value="">Todos los departamentos</option>
                            <c:forEach var="d" items="${departamentos}">
                                <option value="${d.codigo}" ${d.codigo eq filtro ? 'selected' : ''}><c:out value="${d.codigo}" /> - <c:out value="${d.descripcion}" /></option>
                            </c:forEach>
                        </select>
                    </div>
                    <noscript><button class="accion" type="submit">Filtrar</button></noscript>
                </form>
                <div class="gestion-tabla">
                    <table>
                        <thead>
                            <tr><th scope="col">Código</th><th scope="col">Descripción</th><th scope="col">Departamento</th><th scope="col">Acciones</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${cargos}">
                                <tr>
                                    <th scope="row"><c:out value="${item.departamento}${item.codigo}" /></th>
                                    <td><c:out value="${item.descripcion}" default="—" /></td>
                                    <td><c:out value="${item.departamento}" /> <c:out value="${item.departamentoDescripcion}" /></td>
                                    <td class="gestion-acciones">
                                        <a href="${base}/cargos?modo=editar&amp;depto=${item.departamento}&amp;codigo=${item.codigo}#operacion">Editar</a>
                                        <a class="peligro" href="${base}/cargos?modo=eliminar&amp;depto=${item.departamento}&amp;codigo=${item.codigo}#operacion">Eliminar</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty cargos}">
                                <tr><td colspan="4">No hay cargos registrados<c:if test="${not empty filtro}"> en este departamento</c:if>.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="gestion-formulario" aria-labelledby="operacion">
                <c:choose>
                    <c:when test="${modo eq 'editar'}"><h2 id="operacion">Editar cargo</h2></c:when>
                    <c:when test="${modo eq 'eliminar'}"><h2 id="operacion">Eliminar cargo</h2></c:when>
                    <c:otherwise><h2 id="operacion">Nuevo cargo</h2></c:otherwise>
                </c:choose>
                <c:if test="${not empty error}">
                    <p class="alerta" role="alert"><c:out value="${error}" /></p>
                </c:if>
                <form action="${base}/cargos" method="post">
                    <input type="hidden" name="token" value="${token}">
                    <input type="hidden" name="modo" value="${modo}">
                    <c:choose>
                        <c:when test="${modo eq 'eliminar'}">
                            <input type="hidden" name="departamento" value="${cargo.departamento}">
                            <input type="hidden" name="codigo" value="${cargo.codigo}">
                            <p class="gestion-texto">¿Quieres eliminar el cargo <strong><c:out value="${cargo.departamento}" />-<c:out value="${cargo.codigo}" /></strong>?</p>
                            <button class="accion accion-peligro" type="submit">Confirmar eliminación</button>
                        </c:when>
                        <c:otherwise>
                            <c:set var="sel" value="${empty cargo.departamento ? filtro : cargo.departamento}" />
                            <div class="campo">
                                <label for="departamento">Departamento</label>
                                <c:choose>
                                    <c:when test="${modo eq 'editar'}">
                                        <input type="hidden" name="departamento" value="${cargo.departamento}">
                                        <input id="departamento" value="${cargo.departamento} - ${cargo.departamentoDescripcion}" readonly>
                                    </c:when>
                                    <c:otherwise>
                                        <select id="departamento" name="departamento" required>
                                            <option value="">Selecciona un departamento</option>
                                            <c:forEach var="d" items="${departamentos}">
                                                <option value="${d.codigo}" data-siguiente="${siguientes[d.codigo]}" ${d.codigo eq sel ? 'selected' : ''}><c:out value="${d.codigo}" /> - <c:out value="${d.descripcion}" /></option>
                                            </c:forEach>
                                        </select>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="campo">
                                <label for="codigo">Código</label>
                                <c:choose>
                                    <c:when test="${modo eq 'editar'}">
                                        <input id="codigo" name="codigo" value="${cargo.codigo}" readonly>
                                    </c:when>
                                    <c:otherwise>
                                        <input id="codigo" value="${empty sel ? '' : siguientes[sel]}" placeholder="Elige un departamento" readonly>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="campo">
                                <label for="descripcion">Descripción <span>(opcional)</span></label>
                                <input id="descripcion" name="descripcion" value="<c:out value='${cargo.descripcion}' />" maxlength="50">
                            </div>
                            <button class="accion" type="submit">Guardar cargo</button>
                        </c:otherwise>
                    </c:choose>
                </form>
                <c:if test="${modo ne 'crear'}">
                    <a class="enlace-personal gestion-cancelar" href="${base}/cargos">Cancelar</a>
                </c:if>
            </section>
        </div>
    </main>
    <script src="${base}/js/frmcargos.js"></script>
</body>
</html>
