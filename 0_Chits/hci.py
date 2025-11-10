# Q12.py
import tkinter as tk

def say_hello():
    label.config(text="Hello " + name_entry.get() + "!")

root = tk.Tk()
root.title("Simple Python UI")
root.geometry("300x150")

label = tk.Label(root, text="Enter your name:")
label.pack(pady=10)

name_entry = tk.Entry(root)
name_entry.pack(pady=5)

button = tk.Button(root, text="Greet", command=say_hello)
button.pack(pady=10)

root.mainloop()