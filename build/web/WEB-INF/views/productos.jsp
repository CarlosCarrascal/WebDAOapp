<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es" class="dark h-full">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Productos</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body class="bg-gray-900 text-gray-200 antialiased flex flex-col min-h-screen">

    <c:if test="${not empty sessionScope.mensajeExito}">
        <div id="success-alert"
             class="fixed top-4 left-1/2 -translate-x-1/2 z-50 max-w-md w-full px-4 sm:px-0">
            <div class="bg-green-900 border border-green-600 text-green-200 px-4 py-3 rounded-md shadow-lg relative flex items-start" role="alert">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-green-400 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor"> <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" /> </svg>
                <span class="block sm:inline"><c:out value="${sessionScope.mensajeExito}" /></span>
            </div>
        </div>
        <c:remove var="mensajeExito" scope="session" />
    </c:if>

    <div class="container mx-auto px-4 py-6 max-w-7xl flex flex-col flex-grow w-full">

        <header class="pb-3 mb-4 border-b border-gray-700 flex-shrink-0">
            <h1 class="text-4xl font-bold text-white flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 mr-3 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"> <path stroke-linecap="round" stroke-linejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /> </svg>
                 Gestión de Productos
            </h1>
            <p class="mt-1 text-lg text-gray-400 ml-14">Interfaz administrativa para Ventas</p>
        </header>

        <main class="flex-grow flex items-center">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 w-full">

                <%-- Columna Izquierda --%>
                <div class="lg:col-span-1 space-y-4">
                    <div>
                        <c:if test="${not empty errorGeneral}">
                             <div class="bg-red-900 border border-red-600 text-red-200 px-4 py-3 rounded-md relative flex items-start" role="alert">
                                 <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-red-400 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor"> <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" /> </svg>
                                <span class="block sm:inline"><c:out value="${errorGeneral}" /></span>
                             </div>
                         </c:if>
                    </div>

                    <div>
                         <jsp:include page="_formulario_registro.jsp" />
                    </div>
                </div> 

                <%-- Columna Derecha --%>
                <div class="lg:col-span-2 space-y-4">
                    <div>
                        <jsp:include page="_formulario_busqueda.jsp" />
                    </div>
                    <div>
                        <jsp:include page="_tabla_productos.jsp" />
                    </div>
                </div> 

            </div>
        </main> 

    </div>

    <%-- Footer --%>
    <footer class="mt-8 py-4 text-center text-sm text-gray-400 border-t border-gray-700 flex-shrink-0">
        &copy; ${yearNow} - Mi Aplicación de Ventas 2025.
        <jsp:useBean id="yearNowBeanFooter" class="java.util.Date" />
        <fmt:formatDate value="${yearNowBeanFooter}" pattern="yyyy" var="yearNow" />
    </footer>

    <script>
        document.addEventListener('DOMContentLoaded', (event) => {
            const successAlert = document.getElementById('success-alert');
            if (successAlert) {
                setTimeout(() => {
                    successAlert.classList.add('opacity-0', 'transition-opacity', 'duration-500', 'ease-out');

                    setTimeout(() => {
                        successAlert.style.display = 'none';
                    }, 500);
                }, 3000); 
            }
        });
    </script>
</body>
</html>
