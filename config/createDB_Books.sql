-- Set the default DB and drop the BookDB if it exists
USE master;
GO
IF EXISTS(SELECT * FROM sys.databases WHERE name = 'JONSPE01_BookDB')
BEGIN
  DROP DATABASE JONSPE01_BookDB;
END
CREATE DATABASE JONSPE01_BookDB;
GO

-- Set the context to the new BookDB database
USE JONSPE01_BookDB;
GO

-- Create Authors table
CREATE TABLE Authors (
                         AuthorID INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
                         Name VARCHAR(100) NOT NULL,
                         Bio VARCHAR(MAX) NULL
);
GO

-- Create Publishers table
CREATE TABLE Publishers (
                            PublisherID INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
                            Name VARCHAR(100) NOT NULL,
                            Address VARCHAR(255) NULL
);
GO

-- Create Books table
CREATE TABLE Books (
                       BookID INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
                       Title VARCHAR(255) NOT NULL,
                       ISBN VARCHAR(20) NOT NULL,
                       PublicationYear INT NOT NULL,
                       PublisherID INT FOREIGN KEY REFERENCES Publishers(PublisherID),
                       Price DECIMAL(10, 2) NOT NULL
);
GO

-- Create BookAuthors linking table for the many-to-many relationship between Books and Authors
CREATE TABLE BookAuthors (
                             BookID INT FOREIGN KEY REFERENCES Books(BookID),
                             AuthorID INT FOREIGN KEY REFERENCES Authors(AuthorID),
                             PRIMARY KEY (BookID, AuthorID)
);
GO

-- Insert sample data into Publishers
INSERT INTO Publishers (Name, Address) VALUES ('Penguin Random House', 'New York');
INSERT INTO Publishers (Name, Address) VALUES ('HarperCollins', 'New York');
GO

-- Insert sample data into Authors
INSERT INTO Authors (Name, Bio) VALUES ('J.K. Rowling', 'Author of Harry Potter series');
INSERT INTO Authors (Name, Bio) VALUES ('J.R.R. Tolkien', 'Author of The Lord of the Rings');
GO

-- Insert sample data into Books
INSERT INTO Books (Title, ISBN, PublicationYear, PublisherID, Price) VALUES
('Harry Potter and the Philosopher''s Stone', '9780747532699', 1997, (SELECT PublisherID FROM Publishers WHERE Name = 'Penguin Random House'), 20.00),
('The Hobbit', '978004440337', 1937, (SELECT PublisherID FROM Publishers WHERE Name = 'HarperCollins'), 15.00);
GO

-- Insert sample data into BookAuthors
-- Assuming IDs are automatically assigned and incrementally increased, and you know the IDs
INSERT INTO BookAuthors (BookID, AuthorID) VALUES
((SELECT BookID FROM Books WHERE Title = 'Harry Potter and the Philosopher''s Stone'), (SELECT AuthorID FROM Authors WHERE Name = 'J.K. Rowling')),
((SELECT BookID FROM Books WHERE Title = 'The Hobbit'), (SELECT AuthorID FROM Authors WHERE Name = 'J.R.R. Tolkien'));
GO
