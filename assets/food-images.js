(function () {
    const photos = {
        "veg burger": "veg-burger.jpg",
        "cheese burger": "cheese-burger.jpg",
        "paneer burger": "paneer-burger.jpg",
        "veg pizza": "veg-pizza.jpg",
        "margherita pizza": "margherita-pizza.jpg",
        "french fries": "french-fries.jpg",
        "veg sandwich": "veg-sandwich.jpg",
        "grilled cheese sandwich": "grilled-cheese-sandwich.jpg",
        "masala dosa": "masala-dosa.jpg",
        "plain dosa": "plain-dosa.jpg",
        "idli sambhar": "idli-sambhar.jpg",
        "chole bhature": "chole-bhature.jpg",
        "rajma rice": "rajma-rice.jpg",
        "chole rice": "chole-rice.jpg",
        "veg biryani": "veg-biryani.jpg",
        "paneer roll": "paneer-roll.jpg",
        "samosa (2 pieces)": "samosa.jpg",
        "veg momos": "veg-momos.jpg",
        "spring rolls": "spring-rolls.jpg",
        "aloo tikki burger": "aloo-tikki-burger.jpg",
        "cold coffee": "cold-coffee.jpg",
        "masala chai": "masala-chai.jpg",
        "lemon soda": "lemon-soda.jpg",
        "mango shake": "mango-shake.jpg"
    };

    function imageFileFor(name, category) {
        const normalizedName = String(name || "").trim().toLowerCase();
        if (photos[normalizedName]) {
            return photos[normalizedName];
        }

        if (normalizedName.includes("burger")) {
            if (normalizedName.includes("paneer")) return photos["paneer burger"];
            if (normalizedName.includes("cheese")) return photos["cheese burger"];
            if (normalizedName.includes("tikki")) return photos["aloo tikki burger"];
            return photos["veg burger"];
        }
        if (normalizedName.includes("pizza")) {
            return normalizedName.includes("margherita")
                ? photos["margherita pizza"] : photos["veg pizza"];
        }
        if (normalizedName.includes("fries")) return photos["french fries"];
        if (normalizedName.includes("sandwich")) {
            return normalizedName.includes("grilled") || normalizedName.includes("cheese")
                ? photos["grilled cheese sandwich"] : photos["veg sandwich"];
        }
        if (normalizedName.includes("dosa")) {
            return normalizedName.includes("plain") ? photos["plain dosa"] : photos["masala dosa"];
        }
        if (normalizedName.includes("idli")) return photos["idli sambhar"];
        if (normalizedName.includes("bhature")) return photos["chole bhature"];
        if (normalizedName.includes("rajma")) return photos["rajma rice"];
        if (normalizedName.includes("chole") && normalizedName.includes("rice")) {
            return photos["chole rice"];
        }
        if (normalizedName.includes("biryani")) return photos["veg biryani"];
        if (normalizedName.includes("paneer") && normalizedName.includes("roll")) {
            return photos["paneer roll"];
        }
        if (normalizedName.includes("samosa")) return photos["samosa (2 pieces)"];
        if (normalizedName.includes("momo")) return photos["veg momos"];
        if (normalizedName.includes("spring roll")) return photos["spring rolls"];
        if (normalizedName.includes("coffee")) return photos["cold coffee"];
        if (normalizedName.includes("chai") || normalizedName.includes("tea")) {
            return photos["masala chai"];
        }
        if (normalizedName.includes("lemon") || normalizedName.includes("soda")) {
            return photos["lemon soda"];
        }
        if (normalizedName.includes("shake")) return photos["mango shake"];

        switch (category) {
            case "fast-food": return photos["veg burger"];
            case "snacks": return photos["samosa (2 pieces)"];
            case "drinks": return photos["mango shake"];
            default: return photos["chole bhature"];
        }
    }

    function imagePath(name, category) {
        return "assets/food/" + imageFileFor(name, category);
    }

    window.CanteenFoodImages = {
        imageFor: imagePath
    };
}());
