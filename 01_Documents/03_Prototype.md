# Prototype

## QC test

### Prototype cơ bản

Gồm các mục nhập liệu cần thiết cho QC test

- Hình ảnh

![trang QC](./images/qc.png)

- Source

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QC Test Data Entry</title>
    <style>
        body { font-family: sans-serif; margin: 40px; line-height: 1.6; color: #333; }
        form { max-width: 600px; background: #f9f9f9; padding: 20px; border-radius: 8px; border: 1px solid #ddd; }
        div { margin-bottom: 15px; }
        label { display: block; font-weight: bold; margin-bottom: 5px; }
        input, select, textarea { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        button { background-color: #0056b3; color: white; padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
        button:hover { background-color: #004494; }
    </style>
</head>
<body>

    <h2>Quality Control Test Submission</h2>
    
    <form action="https://examplepage.com" method="POST">
        
        <div>
            <label for="lot_id">Lot ID:</label>
            <select name="lot_id" id="lot_id" required>
                <option value="">--Select a Lot--</option>
                <option value="LOT-001">LOT-001</option>
                <option value="LOT-002">LOT-002</option>
                <option value="LOT-003">LOT-003</option>
            </select>
        </div>

        <div>
            <label for="test_type">Test Type:</label>
            <select name="test_type" id="test_type" required>
                <option value="1">Identity</option>
                <option value="2">Potency</option>
                <option value="3">Microbial</option>
                <option value="4">Growth Promotion</option>
                <option value="5">Physical</option>
                <option value="6">Chemical</option>
            </select>
        </div>

        <div>
            <label for="test_method">Test Method:</label>
            <input type="text" name="test_method" id="test_method" placeholder="e.g., USP <123> / Internal SOP-45" required>
        </div>

        <div>
            <label for="test_date">Test Date:</label>
            <input type="date" name="test_date" id="test_date" required>
        </div>

        <div>
            <label for="test_result">Test Result:</label>
            <textarea name="test_result" id="test_result" rows="3" required></textarea>
        </div>

        <div>
            <label for="acceptance_criteria">Acceptance Criteria:</label>
            <textarea type="text" name="acceptance_criteria" id="acceptance_criteria" rows="3"></textarea>
        </div>

        <div>
            <label for="result_status">Result Status:</label>
            <select name="result_status" id="result_status" required>
                <option value="1">Pass</option>
                <option value="2">Fail</option>
                <option value="3">Pending</option>
            </select>
        </div>

        <div>
            <label for="performed_by">Performed By:</label>
            <input type="text" name="performed_by" id="performed_by" required>
        </div>

        <div>
            <label for="verified_by">Verified By:</label>
            <input type="text" name="verified_by" id="verified_by" required>
        </div>

        <button type="submit">Submit QC Data</button>
    </form>

    <script>
        // Automatically set the date picker to today's date
        document.getElementById('test_date').valueAsDate = new Date();
    </script>

</body>
</html>
```
