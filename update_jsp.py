import os

def update_jsp():
    path = r"c:\Users\Soporte y Desarrollo\Documents\NetBeansProjects\combinacion\src\main\webapp\form_supervision.jsp"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    radicacion_html = """
                    <!-- Sección de Radicación -->
                    <c:if test="${not readonly && (empty informe.estadoRadicacion || informe.estadoRadicacion == 'BORRADOR' || informe.estadoRadicacion == 'DEVUELTA')}">
                        <div class="card mt-4 border-primary bg-light">
                            <div class="card-body">
                                <h5 class="card-title text-primary"><i class="bi bi-send-check"></i> Radicar Cuenta de Cobro</h5>
                                <p class="card-text text-muted small">Seleccione la persona encargada de revisar su cuenta y haga clic en Radicar.</p>
                                <div class="row align-items-center">
                                    <div class="col-md-6">
                                        <label class="form-label">Asignar a Revisor:</label>
                                        <select class="form-select" name="id_revisor_asignado" id="revisor_select">
                                            <option value="">-- Seleccione un Revisor --</option>
                                            <c:forEach var="rev" items="${listaRevisores}">
                                                <option value="${rev.id}" ${informe.idRevisorAsignado == rev.id ? 'selected' : ''}>${rev.nombreCompleto}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <input type="hidden" name="radicar" id="radicar_input" value="false">
                            </div>
                        </div>
                    </c:if>
                    
                    <c:if test="${not empty informe.estadoRadicacion}">
                        <div class="alert alert-info mt-4">
                            <strong>Estado actual:</strong> ${informe.estadoRadicacion}
                            <c:if test="${not empty informe.idRevisorAsignado}">
                                <br><small>Revisor asignado ID: ${informe.idRevisorAsignado}</small>
                            </c:if>
                        </div>
                    </c:if>
                    
                    <div class="mt-5 pt-3 border-top d-flex justify-content-end gap-2">"""
                    
    script_html = """
        <script>
            function setRadicar() {
                var rev = document.getElementById("revisor_select").value;
                if (!rev) {
                    Swal.fire('Atención', 'Debe seleccionar un revisor para poder radicar la cuenta.', 'warning');
                    return false;
                }
                document.getElementById("radicar_input").value = "true";
                return true;
            }
        </script>
    </body>"""

    if "<!-- Sección de Radicación -->" not in content:
        content = content.replace(
            '<div class="mt-5 pt-3 border-top d-flex justify-content-end gap-2">',
            radicacion_html
        )
        
        # Add Radicar button inside the buttons area
        content = content.replace(
            '<i class="bi bi-save me-2"></i>${action == \'update\' ? \'Actualizar Informe\' : \'Guardar Informe\'}\n                            </button>',
            '<i class="bi bi-save me-2"></i>${action == \'update\' ? \'Actualizar Informe\' : \'Guardar Informe\'}\n                            </button>\n                            <c:if test="${empty informe.estadoRadicacion || informe.estadoRadicacion == \'BORRADOR\' || informe.estadoRadicacion == \'DEVUELTA\'}">\n                                <button type="submit" class="btn btn-primary px-5 fw-bold shadow-sm" onclick="return setRadicar()">\n                                    <i class="bi bi-send-fill me-2"></i>Radicar Cuenta\n                                </button>\n                            </c:if>'
        )
        
        content = content.replace("</body>", script_html)
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("form_supervision.jsp actualizado.")

if __name__ == '__main__':
    update_jsp()
