var myWidget = cloudinary.createUploadWidget(
  {
    cloudName: 'l8vmmirm',
    uploadPreset: 'uploadimage',
    sources: ['local'],
    resourceType: 'image',
    clientAllowedFormats: ['png', 'jpeg', 'jpg', 'webp'], 
    cropping: true,
    croppingAspectRatio: 4,
    multiple: false,
    croppingShowDimensions: true,
    maxFileSize: 10000000, 
    croppingValidateDimensions: false,
    showSkipCropButton: false,
    singleUploadAutoClose: false,
    
    transformation: [
      { crop: "crop", gravity: "custom" },
      { width: 1584, height: 396, crop: "scale", fetch_format: "auto", quality: "auto" }
    ]
  },
  (error, result) => {
    if (!error && result && result.event === "success") {
      let imageurl = result.info.secure_url;
      if (result.info.coordinates && result.info.coordinates.custom) {
        const cropTransformation = "c_crop,g_custom/c_scale,w_1584,h_396,f_auto,q_auto";
        imageurl = imageurl.replace("/upload/", `/upload/${cropTransformation}/`);
      } else {
        const defaultTransformation = "c_fill,w_1584,h_396,g_auto,f_auto,q_auto";
        imageurl = imageurl.replace("/upload/", `/upload/${defaultTransformation}/`);
      }
      const bannerInput = document.getElementById("duelbanner");
      if (bannerInput) {
        bannerInput.value = imageurl;
      }
      const previewImg = document.getElementById("duelbanner_preview");
      if (previewImg) {
        previewImg.src = imageurl;
        previewImg.style.display = "block";
      }
      
    }
  }
  
);

document.getElementById("upload_widget")?.addEventListener("click", () => {
  myWidget.open();
});
