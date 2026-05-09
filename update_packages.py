import os
import re

root_dir = r'C:\Users\User\Documents\PROYECTO-TFG\slior-project\backend\src\main\java'

replacements = [
    (r'main\.java\.com\.slior', 'com.slior'),
    (r'com\.java\.slior', 'com.slior'),
    (r'package slior', 'package com.slior'),
    (r'import slior', 'import com.slior')
]

for root, dirs, files in os.walk(root_dir):
    for file in files:
        if file.endswith('.java'):
            file_path = os.path.join(root, file)
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            for old, new in replacements:
                new_content = re.sub(old, new, new_content)
            
            if new_content != content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {file_path}")
