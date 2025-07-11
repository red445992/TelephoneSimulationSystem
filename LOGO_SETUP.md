# Logo Setup Instructions

To add your logo to the Telephone Simulation System:

1. **Prepare your logo image:**
   - Name your logo file as "logo.jpg"
   - Recommended size: 150x150 pixels (the application will auto-resize)
   - Supported formats: JPG, PNG, GIF

2. **Place the logo file:**
   - Copy your "logo.jpg" file to the project directory:
     `c:\Users\ASUS\OneDrive\Desktop\aditya\6th\simulationaAndModeling\lab\Telephone-Simulation-System\`
   - Make sure the file is named exactly "logo.jpg" (case-sensitive)

3. **If you don't have a logo:**
   - The application will display "[Logo Image]" as a placeholder
   - The placeholder has a gray border and maintains the proper spacing

4. **To use a different image name or format:**
   - Edit the welcome.java file
   - Find the line: `java.awt.Image img = javax.imageio.ImageIO.read(new java.io.File("logo.jpg"));`
   - Replace "logo.jpg" with your image filename

Note: The logo will be automatically centered and scaled to 150x150 pixels.
