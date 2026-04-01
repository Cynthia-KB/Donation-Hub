<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Contact Us - Phethelo Molapo Foundation</title>
    <link rel="stylesheet" href="style.css">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            color: #333;
            margin: 0;
            padding: 0;
        }
        header, footer {
            background-color: #1f2d3d;
            color: white;
            text-align: center;
            padding: 20px;
        }
        nav {
            background-color: #2c3e50;
            text-align: center;
            padding: 15px;
        }
        nav a {
            color: white;
            margin: 0 15px;
            text-decoration: none;
            font-weight: bold;
        }
        nav a:hover {
            color: #f1c40f;
        }
        .section {
            padding: 50px 20px;
            text-align: center;
        }
        .contact-card {
            max-width: 600px;
            margin: 0 auto;
            background-color: white;
            padding: 30px 20px;
            border-radius: 12px;
            box-shadow: 0 0 15px rgba(0,0,0,0.08);
        }
        .contact-card h2 {
            margin-bottom: 20px;
            color: #1f2d3d;
        }
        .contact-card p {
            margin: 10px 0;
            font-size: 16px;
            line-height: 1.6;
        }
        .contact-card a {
            color: #27ae60;
            text-decoration: none;
        }
        .contact-card a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<header>
    <h1>Phethelo Molapo Foundation</h1>
</header>

<nav>
    <a href="index.html">Home</a>
    <a href="Login.html">Login</a>
    <a href="Register.html">Register</a>
    <a href="donateMoney.html">Donate Money</a>
    <a href="donateItemsLogin.html">Donate Items</a>
    <a href="contact.jsp">Contact</a>
</nav>

<section class="section">
    <div class="contact-card">
        <h2>Contact Us</h2>

        <p><strong>Phone:</strong> <a href="tel:+27604005830">060 400 5830</a></p>

     

        <p><strong>Email:</strong> <a href="mailto:info@phethelofoundation.org">info@phethelofoundation.org</a></p>

        <p><strong>Physical Address:</strong><br>
           kwa-Molapo, Ncotshane,<br>
           Pongola</p>
    </div>
    
      <div class="volunteer">
            <h3>Volunteer with Us!</h3>
            <p>Want to help our community? Join our WhatsApp group to volunteer and stay updated on events:</p>
            <p><a href="https://chat.whatsapp.com/YourGroupInviteLink" target="_blank">Join Volunteer WhatsApp Group</a></p>
        </div>
    
</section>

<footer>
    <p>&copy; 2026 Phethelo Molapo Foundation | All Rights Reserved</p>
</footer>

</body>
</html>