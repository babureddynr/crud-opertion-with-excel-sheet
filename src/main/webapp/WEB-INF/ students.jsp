<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Student Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <!-- Alert Messages -->
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <span>${message}</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <span>${error}</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- Bulk Upload Section -->
        <div class="card mb-4">
            <div class="card-header">
                <h4>Bulk Student Upload</h4>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6">
                        <a href="<c:url value='/students/download-template'/>" class="btn btn-primary mb-3"> Download Excel Template </a>
                        <a href="<c:url value='/students/download-all'/>" class="btn btn-primary"> Download All Students Excel </a>
                    </div>
                </div>
                <form action="<c:url value='/students/upload'/>" method="post" enctype="multipart/form-data">
                    <div class="input-group mb-3">
                        <input type="file" name="file" class="form-control" accept=".xlsx" required>
                        <button class="btn btn-success" type="submit">Upload</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Individual Student Form -->
        <div class="card mb-4">
            <div class="card-header">
                <h4>Add New Student</h4>
            </div>
            <div class="card-body">
                <form action="<c:url value='/students/save'/>" method="post">
                    <div class="mb-3">
                        <label for="name" class="form-label">Name</label>
                        <input type="text" class="form-control" id="name" name="name" value="${student.name}">
                        <c:if test="${not empty student.errors.name}">
                            <div class="text-danger">${student.errors.name}</div>
                        </c:if>
                    </div>

                    <div class="mb-3">
                        <label for="address" class="form-label">Address</label>
                        <input type="text" class="form-control" id="address" name="address" value="${student.address}">
                        <c:if test="${not empty student.errors.address}">
                            <div class="text-danger">${student.errors.address}</div>
                        </c:if>
                    </div>

                    <div class="mb-3">
                        <label for="image" class="form-label">Image URL</label>
                        <input type="text" class="form-control" id="image" name="image" value="${student.image}">
                    </div>

                    <button type="submit" class="btn btn-primary">Save Student</button>
                </form>
            </div>
        </div>

        <!-- Students List -->
        <div class="card">
            <div class="card-header">
                <h4>Students List</h4>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Address</th>
                                <th>Image</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="student" items="${students}">
                                <tr>
                                    <td>${student.id}</td>
                                    <td>${student.name}</td>
                                    <td>${student.address}</td>
                                    <td>
                                        <c:if test="${not empty student.image}">
                                            <img src="${student.image}" alt="Student Image" style="max-width: 50px;">
                                        </c:if>
                                    </td>
                                    <td>
                                        <button type="button" class="btn btn-warning btn-sm" data-bs-toggle="modal" data-bs-target="#editModal${student.id}">Edit</button>
                                        <a href="<c:url value='/students/delete/${student.id}'/>" class="btn btn-danger btn-sm" onclick="return confirm('Are you sure you want to delete this student?')">Delete</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Edit Modals -->
        <c:forEach var="student" items="${students}">
            <div id="editModal${student.id}" class="modal fade" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Edit Student</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <form action="<c:url value='/students/update/${student.id}'/>" method="post">
                            <div class="modal-body">
                                <div class="mb-3">
                                    <label for="name" class="form-label">Name</label>
                                    <input type="text" class="form-control" name="name" value="${student.name}" required>
                                </div>
                                <div class="mb-3">
                                    <label for="address" class="form-label">Address</label>
                                    <input type="text" class="form-control" name="address" value="${student.address}" required>
                                </div>
                                <div class="mb-3">
                                    <label for="image" class="form-label">Image URL</label>
                                    <input type="text" class="form-control" name="image" value="${student.image}">
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                                <button type="submit" class="btn btn-primary">Save changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
