const sharp = require('sharp');

async function removeBg(input, output) {
  try {
      const { data, info } = await sharp(input)
        .ensureAlpha()
        .raw()
        .toBuffer({ resolveWithObject: true });
        
      for (let i = 0; i < data.length; i += 4) {
        if (data[i] < 30 && data[i+1] < 30 && data[i+2] < 30) {
          data[i+3] = 0; // alpha
        }
      }
      
      await sharp(data, { raw: { width: info.width, height: info.height, channels: 4 } })
        .png()
        .toFile(output);
      console.log('Saved', output);
  } catch(e) {
      console.error(e);
  }
}

async function main() {
    await removeBg("app/src/main/res/drawable/logo_stikes.jpg", "app/src/main/res/drawable/logo_stikes.png");
    await removeBg("web/public/logo_stikes.jpg", "web/public/logo_stikes.png");
}

main();
