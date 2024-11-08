<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Student Management</title>
</head>
<body>
    <h2>Student Management</h2>

    <!-- Display Table of Students -->
    <table border="1" cellpadding="10" cellspacing="0">
        <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Address</th>
                <th>Image</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="student" items="${students}">
                <tr>
                    <td>${student.id}</td>
                    <td>${student.name}</td>
                    <td>${student.address}</td>
                    <td><img src="${student.image}" alt="Image" width="50" height="50"></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <h3>Add New Student</h3>
    
    <!-- Form to Add New Student -->
    <form action="${pageContext.request.contextPath}/student/add" method="post">
        <label for="name">Name:</label>
        <input type="text" id="name" name="name" required><br><br>

        <label for="address">Address:</label>
        <input type="text" id="address" name="address" required><br><br>

        <label for="image">Image URL:</label>
        <input type="text" id="image" name="image"><br><br>

        <input type="submit" value="Add Student">
    </form>

</body>
</html>
