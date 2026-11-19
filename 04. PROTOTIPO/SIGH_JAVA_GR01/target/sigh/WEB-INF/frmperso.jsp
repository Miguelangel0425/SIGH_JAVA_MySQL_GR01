<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="cod" value="${nuevo ? '' : persona.codigo}" />
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Personas | SIGH</title>
    <link rel="stylesheet" href="${base}/css/departamentos.css">
    <link rel="stylesheet" href="${base}/css/persona.css">
</head>
<body>
    <main class="gestion ficha">
        <header class="gestion-cabecera">
            <div>
                <p class="gestion-etiqueta">SIGH · SUBSISTEMA DE PERSONAL</p>
                <h1>Mantenimiento de Personal</h1>
            </div>
            <div class="gestion-acciones no-imprimir">
                <a class="enlace-personal" href="${base}/inicio">Inicio</a>
                <a class="enlace-personal" href="${base}/departamentos">Departamentos</a>
                <a class="enlace-personal" href="${base}/cargos">Cargos</a>
                <form action="${base}/salir" method="post">
                    <input type="hidden" name="token" value="${token}">
                    <button class="accion" type="submit">Cerrar sesión</button>
                </form>
            </div>
        </header>

        <div class="ficha-cuerpo">
            <c:if test="${not empty aviso}"><p class="aviso" role="status"><c:out value="${aviso}" /></p></c:if>
            <c:if test="${not empty error}"><p class="alerta" role="alert"><c:out value="${error}" /></p></c:if>

            <c:if test="${bloqueado}">
                <p class="aviso">Pulsa Nuevo para registrar una persona, o Listar para elegir una y actualizarla.</p>
            </c:if>

            <form id="frmpersona" action="${base}/persona" method="post" enctype="multipart/form-data" data-bloqueado="${bloqueado}">
                <input type="hidden" name="token" value="${token}">
                <input type="hidden" name="codigo" value="${cod}">
                <input type="hidden" id="tab" name="tab" value="${tab}">

                <%-- CABECERA: queda siempre visible, fuera de las pestañas --%>
                <section class="ficha-cabecera" aria-label="Datos de la Persona">
                    <div class="ficha-campos">
                        <div class="campo">
                            <label for="codigo">Código</label>
                            <input id="codigo" value="<c:out value='${nuevo ? (bloqueado ? "" : siguiente) : persona.codigo}' />" readonly>
                        </div>
                        <div class="campo">
                            <label for="apellidos">Apellidos</label>
                            <input id="apellidos" name="apellidos" value="<c:out value='${persona.apellidos}' />" maxlength="50" required>
                        </div>
                        <div class="campo">
                            <label for="nombres">Nombres</label>
                            <input id="nombres" name="nombres" value="<c:out value='${persona.nombres}' />" maxlength="50" required>
                        </div>
                    </div>
                    <div class="ficha-foto">
                        <img id="vista-foto" src="${base}/foto?archivo=<c:out value='${empty persona.foto ? "default.png" : persona.foto}' />" alt="Foto de la Persona" width="180" height="180">
                        <label class="no-imprimir">Foto (PNG o JPG, máx. 2 MB)
                            <input id="foto" name="foto" type="file" accept="image/png,image/jpeg">
                        </label>
                    </div>
                </section>

                <div class="ficha-principal">
                    <div>
                        <div class="pestanas no-imprimir" role="tablist" aria-label="Datos de la Persona" hidden>
                            <button type="button" role="tab" id="t-generales" aria-controls="generales" data-panel="generales">Datos generales</button>
                            <button type="button" role="tab" id="t-identificacion" aria-controls="identificacion" data-panel="identificacion">Identificación y contacto</button>
                        </div>

                        <section id="generales" class="panel-pestana" role="tabpanel" aria-labelledby="t-generales">
                            <h2 class="solo-imprimir">Datos generales</h2>
                            <div class="campo">
                                <label for="genero">Sexo</label>
                                <select id="genero" name="genero" required>
                                    <option value="">Selecciona</option>
                                    <c:forEach var="o" items="${generos}">
                                        <option value="${o.codigo}" ${o.codigo eq persona.genero ? 'selected' : ''}><c:out value="${o.descripcion}" /></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="campo">
                                <label for="estadoCivil">Estado civil</label>
                                <select id="estadoCivil" name="estadoCivil" required>
                                    <option value="">Selecciona</option>
                                    <c:forEach var="o" items="${estadosCiviles}">
                                        <option value="${o.codigo}" ${o.codigo eq persona.estadoCivil ? 'selected' : ''}><c:out value="${o.descripcion}" /></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="campo">
                                <label for="fechaNacimiento">Fecha de nacimiento</label>
                                <input id="fechaNacimiento" name="fechaNacimiento" type="date" value="${persona.fechaNacimiento}" required>
                            </div>
                            <div class="campo">
                                <label for="cargasFamiliares">Cargas familiares</label>
                                <input id="cargasFamiliares" name="cargasFamiliares" type="number" min="0" max="99" value="${empty persona ? 0 : persona.cargasFamiliares}">
                            </div>
                            <div class="campo campo-marca">
                                <label><input type="checkbox" name="discapacidad" value="S" ${persona.discapacidad eq 'S' ? 'checked' : ''}> Tiene discapacidad</label>
                            </div>
                        </section>

                        <section id="identificacion" class="panel-pestana" role="tabpanel" aria-labelledby="t-identificacion">
                            <h2 class="solo-imprimir">Identificación y contacto</h2>
                            <c:set var="tipoDoc" value="${not empty tipoDocumento ? tipoDocumento : (not empty persona.pasaporte and empty persona.cedula ? 'PASAPORTE' : 'CEDULA')}" />
                            <div class="campo">
                                <label for="tipoDocumento">Tipo de documento</label>
                                <select id="tipoDocumento" name="tipoDocumento" required>
                                    <option value="CEDULA" ${tipoDoc eq 'CEDULA' ? 'selected' : ''}>Cédula</option>
                                    <option value="PASAPORTE" ${tipoDoc eq 'PASAPORTE' ? 'selected' : ''}>Pasaporte</option>
                                </select>
                            </div>
                            <div class="campo">
                                <label for="documento" id="etiqueta-documento">${tipoDoc eq 'PASAPORTE' ? 'Número de pasaporte' : 'Número de cédula'}</label>
                                <input id="documento" name="documento" value="<c:out value='${empty persona.cedula ? persona.pasaporte : persona.cedula}' />"
                                       maxlength="${tipoDoc eq 'PASAPORTE' ? 15 : 10}" pattern="${tipoDoc eq 'PASAPORTE' ? '[A-Za-z0-9]{1,15}' : '[0-9]{10}'}" required>
                            </div>
                            <div class="campo campo-ancho">
                                <label for="email">Correo electrónico</label>
                                <input id="email" name="email" type="email" value="<c:out value='${persona.email}' />" maxlength="50">
                            </div>
                        </section>
                    </div>

                    <nav class="ficha-nav no-imprimir" aria-label="Navegar entre persona">
                        <a href="${base}/persona?nav=primero&amp;codigo=${cod}&amp;tab=${tab}" title="Primero" aria-label="Primero">⏮</a>
                        <a href="${base}/persona?nav=anterior&amp;codigo=${cod}&amp;tab=${tab}" title="Anterior" aria-label="Anterior">◀</a>
                        <a href="${base}/persona?nav=siguiente&amp;codigo=${cod}&amp;tab=${tab}" title="Siguiente" aria-label="Siguiente">▶</a>
                        <a href="${base}/persona?nav=ultimo&amp;codigo=${cod}&amp;tab=${tab}" title="Último" aria-label="Último">⏭</a>
                    </nav>
                </div>

                <div class="botonera no-imprimir">
                    <a class="accion accion-secundaria" href="${base}/persona?modo=nuevo&amp;tab=${tab}">Nuevo</a>
                    <a class="accion accion-secundaria" href="${base}/persona?modo=listar&amp;codigo=${cod}&amp;tab=${tab}#listado">Listar</a>
                    <button class="accion" type="submit" name="accion" value="guardar"><c:out value="${nuevo ? 'Guardar' : 'Actualizar'}" /></button>
                    <c:if test="${not nuevo}">
                        <button class="accion accion-peligro" type="submit" name="accion" value="borrar" formnovalidate data-confirmar="¿Eliminar a la persona ${persona.codigo}?">Borrar</button>
                    </c:if>
                    <a class="accion accion-secundaria" href="${base}/reporte?r=personas" target="_blank" rel="noopener">Listado PDF</a>
                    <%-- Imprimir: genera el reporte (PDF) de la persona seleccionada --%>
                    <c:choose>
                        <c:when test="${not nuevo}">
                            <a class="accion accion-secundaria" href="${base}/reporte?r=ficha&amp;codigo=${persona.codigo}" target="_blank" rel="noopener">Imprimir</a>
                        </c:when>
                        <c:otherwise>
                            <button class="accion accion-secundaria" type="button" disabled title="Selecciona una persona para imprimir su ficha">Imprimir</button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </form>

            <c:if test="${not empty lista}">
                <section class="no-imprimir" aria-labelledby="listado">
                    <h2 id="listado">Personas registradas</h2>
                    <div class="gestion-tabla">
                        <table>
                            <thead><tr><th scope="col">Código</th><th scope="col">Apellidos</th><th scope="col">Nombres</th><th scope="col">Documento</th><th scope="col"></th></tr></thead>
                            <tbody>
                                <c:forEach var="e" items="${lista}">
                                    <tr>
                                        <th scope="row"><c:out value="${e.codigo}" /></th>
                                        <td><c:out value="${e.apellidos}" /></td>
                                        <td><c:out value="${e.nombres}" /></td>
                                        <td><c:out value="${empty e.cedula ? e.pasaporte : e.cedula}" default="—" /></td>
                                        <td class="gestion-acciones"><a href="${base}/persona?codigo=${e.codigo}&amp;tab=${tab}">Seleccionar</a></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
            </c:if>
        </div>
    </main>
    <script src="${base}/js/persona.js"></script>
</body>
</html>
