from PIL import Image

def remove_black_background(input_path, output_path, tolerance=30):
    img = Image.open(input_path).convert("RGBA")
    datas = img.getdata()
    
    new_data = []
    for item in datas:
        # Check if the pixel is black or near-black
        if item[0] < tolerance and item[1] < tolerance and item[2] < tolerance:
            # Change to transparent
            new_data.append((255, 255, 255, 0))
        else:
            new_data.append(item)
            
    img.putdata(new_data)
    img.save(output_path, "PNG")

# Android Logo
remove_black_background(
    r"e:\Project\campusexam-sep-8-2026-2-49-47-pm\app\src\main\res\drawable\logo_stikes.jpg",
    r"e:\Project\campusexam-sep-8-2026-2-49-47-pm\app\src\main\res\drawable\logo_stikes.png",
    tolerance=30
)

# Web Logo
remove_black_background(
    r"e:\Project\campusexam-sep-8-2026-2-49-47-pm\web\public\logo_stikes.jpg",
    r"e:\Project\campusexam-sep-8-2026-2-49-47-pm\web\public\logo_stikes.png",
    tolerance=30
)
print("Background removed successfully!")
