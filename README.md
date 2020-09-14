# image-4-points-crop

command line tool for cropping an image using four points, adjust the view perspective of the cropped image.

## dependences

**Apache Commons CLI**  
https://commons.apache.org/proper/commons-cli/

**Apache Commons Math**  
http://commons.apache.org/proper/commons-math/

#### installation
download the libraries archive from the website and extract it where you wish to install it(usually in a lib directory within your project)  
add the libraries to your project by **clicking on your project -> Propeties -> Java Build Path -> Libraries Tab**  
click on **Add External JARs...** navigate to your library installation directory and click on **commons-cli-1.4.jar** or **commons-math3-3.6.1.jar**  
for **Apache Commons CLI** or **Apache Commons Math** library respectively, finally click on ok to save changes.

## command line
```
Usage: java -jar bin.jar [options] -c <list of four (x,y) coordinates> -i <file>  
  
crop an image using four points and adjust view perspective.  
if output path was not provided the result image will be stored in the folder of the input image  
as [input image name]-4crop.[format]  
  
 -c,--coords <list of four (x,y) coordinates>   coordinates that represent a rectangular shape,  
                                                arraignment is not relevant.  
                                                example: [(3,3),(16,56),(73,55),(62,14)]  
                                                 note: if list contain spaces it should be  
                                                 encapsulated with parentheses  
 -h,--help                                      print this message  
 -i,--image <file>                              input image path  
 -o,--output-image <file>                       output image path  
    --output-format <format=png>                choose output format jpg or png(default)  
```

## Example

| original     | cropped       |
|--------------|---------------| 
|![Alt text](example/IMG_20200914_195400.jpg?raw=true "original") | ![Alt text](example/IMG_20200914_195400-4crop.jpg?raw=true "cropped")|
